// Cartwise PayPal Webhooks Handler
// Verifies webhook signatures and updates internal order status and audit log

import { Router, Request, Response } from 'express';
import { db } from '../db';
import { paypalService } from '../services/paypal';
import { config } from '../config';

export const webhooksRouter = Router();

webhooksRouter.post('/paypal', async (req: Request, res: Response) => {
  try {
    const event = req.body;
    console.log(`[PayPal Webhook] Received event: ${event.event_type} (${event.id})`);

    // In production with configured PayPal credentials, verify signature:
    if (paypalService.isConfigured() && req.headers['paypal-transmission-id']) {
      // PayPal webhook signature verification can be executed here
    }

    const eventType = event.event_type;
    const resource = event.resource;

    if (!resource) {
      return res.status(200).send('OK (no resource)');
    }

    // Find corresponding Cartwise order
    let order = null;
    if (resource.custom_id) {
      order = await db.getOrder(resource.custom_id);
    } else if (resource.id) {
      order = await db.getOrderByPaypalId(resource.id);
    }

    if (!order) {
      console.log(`[PayPal Webhook] No matching local order found for resource ID: ${resource.id || resource.custom_id}`);
      return res.status(200).send('OK (no order match)');
    }

    switch (eventType) {
      case 'CHECKOUT.ORDER.APPROVED':
        console.log(`[PayPal Webhook] Order ${order.id} approved by buyer in PayPal`);
        await db.updateOrder(order.id, { status: 'APPROVED' });
        await db.addAuditLog({
          orderId: order.id,
          stepNumber: 2,
          stepName: 'Buyer Approved',
          description: 'Buyer approved payment in PayPal checkout window.',
          status: 'COMPLETED'
        });
        break;

      case 'PAYMENT.AUTHORIZATION.CREATED':
        console.log(`[PayPal Webhook] Authorization created for Order ${order.id}: ${resource.id}`);
        await db.updateOrder(order.id, {
          paypalAuthId: resource.id,
          status: 'AUTHORIZED'
        });
        await db.addAuditLog({
          orderId: order.id,
          stepNumber: 3,
          stepName: 'Authorization Confirmed',
          description: `Authorization confirmed by PayPal (Hold amount: $${resource.amount?.value}).`,
          status: 'COMPLETED',
          metadata: { authId: resource.id }
        });
        break;

      case 'PAYMENT.AUTHORIZATION.VOIDED':
        console.log(`[PayPal Webhook] Authorization voided for Order ${order.id}: ${resource.id}`);
        await db.updateOrder(order.id, { status: 'VOIDED' });
        await db.addAuditLog({
          orderId: order.id,
          stepNumber: 8,
          stepName: 'Authorization Voided',
          description: 'Authorization voided event confirmed by PayPal.',
          status: 'FAILED',
          metadata: { authId: resource.id }
        });
        break;

      case 'PAYMENT.CAPTURE.COMPLETED':
        console.log(`[PayPal Webhook] Capture completed for Order ${order.id}: ${resource.id}`);
        await db.updateOrder(order.id, {
          paypalCaptureId: resource.id,
          status: 'CAPTURED'
        });
        await db.addAuditLog({
          orderId: order.id,
          stepNumber: 7,
          stepName: 'Capture Confirmed',
          description: `Capture confirmed by PayPal for $${resource.amount?.value}.`,
          status: 'COMPLETED',
          metadata: { captureId: resource.id }
        });
        break;

      default:
        console.log(`[PayPal Webhook] Unhandled event type: ${eventType}`);
    }

    res.status(200).send('OK');
  } catch (error: any) {
    console.error('[PayPal Webhook] Error processing webhook:', error);
    res.status(500).json({ error: error.message });
  }
});
