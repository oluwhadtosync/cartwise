// Cartwise Orders Routes
// Handles order creation with PayPal AUTHORIZE intent, authorization execution, capture, and void.

import { Router, Request, Response } from 'express';
import { db } from '../db';
import { paypalService } from '../services/paypal';
import { CartwiseOrder } from '../types';
import { config } from '../config';

export const ordersRouter = Router();

// Create new Cartwise Order & initiate PayPal authorization
ordersRouter.post('/', async (req: Request, res: Response) => {
  try {
    const {
      userId = 'user_default',
      itemTitle,
      itemUrl,
      itemPrice,
      shippingCost = 5.00,
      merchantName = 'Demo Market',
      shippingAddress = {
        fullName: 'Ada Lovelace',
        email: 'ada@cartwise.shopper',
        address: '14 Marina Street, Lagos Island',
        city: 'Lagos',
        state: 'Lagos',
        country: 'Nigeria',
        postalCode: '100001'
      },
      billingAddress,
      selectedVariety,
      client,
      clientOrigin,
      returnUrl: clientReturnUrl,
      cancelUrl: clientCancelUrl
    } = req.body;

    if (!itemTitle || itemPrice === undefined) {
      return res.status(400).json({ error: 'itemTitle and itemPrice are required' });
    }

    const price = Number(Number(itemPrice).toFixed(2));
    const shipping = Number(Number(shippingCost).toFixed(2));
    const serviceFee = config.paypal.serviceFee;
    const totalAmount = Number((price + shipping + serviceFee).toFixed(2));
    const orderId = `cw_ord_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`;

    // Client platform identification:
    // If initiated from Web, return to Web URL. If from Android, return to cartwise:// deep link.
    const userAgent = (req.headers['user-agent'] || '').toLowerCase();
    const isExplicitAndroid = client === 'android' || userAgent.includes('okhttp') || userAgent.includes('cartwise-android');
    const isExplicitWeb = client === 'web';
    const isAndroid = isExplicitAndroid && !isExplicitWeb;
    const isWeb = isExplicitWeb || !isAndroid;

    let returnUrl = clientReturnUrl;
    let cancelUrl = clientCancelUrl;

    if (!returnUrl) {
      if (isWeb) {
        let webOrigin = clientOrigin;
        if (!webOrigin && req.headers.origin) {
          webOrigin = req.headers.origin;
        } else if (!webOrigin && req.headers['x-forwarded-host']) {
          const proto = req.headers['x-forwarded-proto'] || 'http';
          webOrigin = `${proto}://${req.headers['x-forwarded-host']}`;
        } else if (!webOrigin && req.headers.referer) {
          try {
            webOrigin = new URL(req.headers.referer).origin;
          } catch (e) {
            // ignore
          }
        }
        if (!webOrigin || webOrigin === 'null') {
          webOrigin = config.frontendUrl || 'https://cartwise-web-app.onrender.com';
        }
        returnUrl = `${webOrigin}/?orderId=${orderId}&paypal_return=true`;
      } else {
        returnUrl = `cartwise://paypal/return?orderId=${orderId}`;
      }
    }

    if (!cancelUrl) {
      if (isWeb) {
        let webOrigin = clientOrigin;
        if (!webOrigin && req.headers.origin) {
          webOrigin = req.headers.origin;
        } else if (!webOrigin && req.headers['x-forwarded-host']) {
          const proto = req.headers['x-forwarded-proto'] || 'http';
          webOrigin = `${proto}://${req.headers['x-forwarded-host']}`;
        } else if (!webOrigin && req.headers.referer) {
          try {
            webOrigin = new URL(req.headers.referer).origin;
          } catch (e) {
            // ignore
          }
        }
        if (!webOrigin || webOrigin === 'null') {
          webOrigin = config.frontendUrl || 'https://cartwise-web-app.onrender.com';
        }
        cancelUrl = `${webOrigin}/?orderId=${orderId}&paypal_cancel=true`;
      } else {
        cancelUrl = `cartwise://paypal/cancel?orderId=${orderId}`;
      }
    }

    // Create PayPal Order with intent="AUTHORIZE"
    const ppOrder = await paypalService.createAuthorizeOrder({
      orderId,
      itemTitle,
      itemPrice: price,
      shippingCost: shipping,
      serviceFee,
      currency: config.paypal.currency,
      returnUrl,
      cancelUrl
    });

    const newOrder: CartwiseOrder = {
      id: orderId,
      userId,
      itemTitle,
      itemUrl: itemUrl || '',
      itemPrice: price,
      shippingCost: shipping,
      serviceFee,
      totalAmount,
      currency: config.paypal.currency,
      merchantName,
      selectedVariety,
      client: isWeb ? 'web' : 'android',
      paypalOrderId: ppOrder.paypalOrderId,
      paypalApproveUrl: ppOrder.approveUrl,
      status: 'PENDING_APPROVAL',
      shippingAddress,
      billingAddress,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };

    await db.createOrder(newOrder);

    await db.addAuditLog({
      orderId,
      stepNumber: 1,
      stepName: 'Order Created',
      description: `Order initiated for "${itemTitle}". Total: $${totalAmount.toFixed(2)} ($${price.toFixed(2)} item + $${shipping.toFixed(2)} shipping + $${serviceFee.toFixed(2)} fee).`,
      status: 'COMPLETED',
      metadata: { paypalOrderId: ppOrder.paypalOrderId }
    });

    res.status(201).json(newOrder);
  } catch (error: any) {
    console.error('Error creating order:', error);
    res.status(500).json({ error: error.message || 'Failed to create order' });
  }
});

// Authorize PayPal payment after buyer approval
ordersRouter.post('/:id/authorize', async (req: Request, res: Response) => {
  try {
    const order = await db.getOrder(req.params.id);
    if (!order) {
      return res.status(404).json({ error: 'Order not found' });
    }

    if (!order.paypalOrderId) {
      return res.status(400).json({ error: 'No PayPal order ID associated with this order' });
    }

    // Call PayPal Orders v2 Authorize
    const authResult = await paypalService.authorizeOrder(order.paypalOrderId);

    const updated = await db.updateOrder(order.id, {
      paypalAuthId: authResult.authorizationId,
      status: 'AUTHORIZED'
    });

    await db.addAuditLog({
      orderId: order.id,
      stepNumber: 2,
      stepName: 'PayPal Authorized',
      description: `Payment authorized via PayPal. Funds held in escrow (Auth ID: ${authResult.authorizationId}).`,
      status: 'COMPLETED',
      metadata: { authId: authResult.authorizationId, authStatus: authResult.status }
    });

    res.json(updated);
  } catch (error: any) {
    console.error('Error authorizing order:', error);
    res.status(500).json({ error: error.message || 'Failed to authorize PayPal order' });
  }
});

// Capture PayPal authorization (Executed upon store order confirmation)
ordersRouter.post('/:id/capture', async (req: Request, res: Response) => {
  try {
    const order = await db.getOrder(req.params.id);
    if (!order) {
      return res.status(404).json({ error: 'Order not found' });
    }

    if (!order.paypalAuthId) {
      return res.status(400).json({ error: 'Order does not have a PayPal authorization to capture' });
    }

    const captureResult = await paypalService.captureAuthorization(
      order.paypalAuthId,
      order.totalAmount,
      order.currency
    );

    const updated = await db.updateOrder(order.id, {
      paypalCaptureId: captureResult.captureId,
      status: 'CAPTURED'
    });

    await db.addAuditLog({
      orderId: order.id,
      stepNumber: 7,
      stepName: 'Payment Captured',
      description: `Funds successfully captured on PayPal (Capture ID: ${captureResult.captureId}). Order complete.`,
      status: 'COMPLETED',
      metadata: { captureId: captureResult.captureId }
    });

    res.json(updated);
  } catch (error: any) {
    console.error('Error capturing order:', error);
    res.status(500).json({ error: error.message || 'Failed to capture PayPal authorization' });
  }
});

// Void PayPal authorization (Executed when store checkout fails or item is out of stock)
ordersRouter.post('/:id/void', async (req: Request, res: Response) => {
  try {
    const order = await db.getOrder(req.params.id);
    if (!order) {
      return res.status(404).json({ error: 'Order not found' });
    }

    if (!order.paypalAuthId) {
      return res.status(400).json({ error: 'Order does not have an active PayPal authorization' });
    }

    const { reason = 'Order cancelled or merchant checkout failed' } = req.body;

    await paypalService.voidAuthorization(order.paypalAuthId);

    const updated = await db.updateOrder(order.id, {
      status: 'VOIDED',
      failureReason: reason
    });

    await db.addAuditLog({
      orderId: order.id,
      stepNumber: 8,
      stepName: 'Authorization Voided',
      description: `PayPal hold voided and released back to buyer: ${reason}. You were NOT charged.`,
      status: 'FAILED',
      metadata: { reason }
    });

    res.json(updated);
  } catch (error: any) {
    console.error('Error voiding authorization:', error);
    res.status(500).json({ error: error.message || 'Failed to void PayPal authorization' });
  }
});

// Get order details with timeline audit logs
ordersRouter.get('/:id', async (req: Request, res: Response) => {
  try {
    const order = await db.getOrder(req.params.id);
    if (!order) {
      return res.status(404).json({ error: 'Order not found' });
    }
    const auditLogs = await db.getAuditLogs(order.id);
    res.json({
      order,
      timeline: auditLogs
    });
  } catch (error: any) {
    res.status(500).json({ error: error.message });
  }
});

// List orders
ordersRouter.get('/', async (req: Request, res: Response) => {
  try {
    const userId = req.query.userId as string;
    const orders = await db.listOrders(userId);
    res.json({ orders, total: orders.length });
  } catch (error: any) {
    res.status(500).json({ error: error.message });
  }
});

// Mock PayPal Approval Page (Helper for testing approval in web browser or demo mode)
ordersRouter.get('/mock-approve', (req: Request, res: Response) => {
  const { orderId, paypalOrderId, returnUrl } = req.query;
  const redirectTarget = returnUrl ? String(returnUrl) : `/?orderId=${orderId}&paypal_return=true`;

  const html = `<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>PayPal Sandbox Approval Simulation</title>
  <style>
    body { font-family: system-ui, sans-serif; background: #f4f6f8; padding: 2rem; display: flex; justify-content: center; align-items: center; min-height: 80vh; }
    .card { background: #fff; padding: 2.5rem; border-radius: 1rem; box-shadow: 0 10px 25px rgba(0,0,0,0.1); max-width: 480px; width: 100%; text-align: center; }
    .btn { background: #FFC439; color: #003087; font-weight: 700; border: 1px solid #E0A820; padding: 0.85rem 1.5rem; border-radius: 2rem; cursor: pointer; font-size: 1rem; width: 100%; margin-top: 1.5rem; transition: background 0.2s; }
    .btn:hover { background: #F4B400; }
    .badge { background: #003087; color: #fff; padding: 0.25rem 0.6rem; border-radius: 9999px; font-size: 0.75rem; font-weight: 800; text-transform: uppercase; letter-spacing: 0.5px; }
  </style>
</head>
<body>
  <div class="card">
    <div style="font-size: 2.5rem; margin-bottom: 0.5rem;">🅿️</div>
    <h2 style="color: #003087; margin-bottom: 0.25rem;">PayPal <span style="color: #0079C1;">Sandbox</span></h2>
    <span class="badge">Sandbox Checkout</span>
    <p style="color: #555; margin: 1.5rem 0; font-size: 0.95rem;">
      You are authorizing a payment hold for Cartwise Order <strong>${orderId}</strong>.
      Funds will only be captured if the store confirms the order.
    </p>
    <div style="background: #f8f9fa; border: 1px solid #e9ecef; padding: 1rem; border-radius: 0.5rem; font-size: 0.85rem; text-align: left; margin-bottom: 1rem;">
      <div><strong>PayPal Order ID:</strong> ${paypalOrderId}</div>
      <div><strong>Payment Intent:</strong> AUTHORIZE</div>
      <div><strong>Buyer Protection:</strong> Eligible</div>
    </div>
    <button class="btn" onclick="approve()">Pay with PayPal &rarr; Return to Cartwise</button>
  </div>
  <script>
    async function approve() {
      const btn = document.querySelector('button');
      btn.innerText = 'Authorizing Escrow...';
      btn.disabled = true;
      try {
        const res = await fetch('/api/orders/${orderId}/authorize', { method: 'POST' });
        const data = await res.json();
        window.location.href = "${redirectTarget}";
      } catch (e) {
        alert('Error: ' + e.message);
      }
    }
  </script>
</body>
</html>`;

  res.send(html);
});
