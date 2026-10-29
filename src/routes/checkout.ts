// Cartwise Checkout Routes with Server-Sent Events (SSE) Streaming
// Streams step-by-step progress, Kernel live view URL, and final results in real time

import { Router, Request, Response } from 'express';
import { db } from '../db';
import { kernelAgent } from '../services/kernelAgent';

export const checkoutRouter = Router();

// Store active SSE listener response objects by orderId
const sseClients: Map<string, Response[]> = new Map();

function broadcastEvent(orderId: string, eventData: any) {
  const clients = sseClients.get(orderId) || [];
  const payload = `data: ${JSON.stringify(eventData)}\n\n`;
  for (const client of clients) {
    client.write(payload);
  }
}

// SSE Stream endpoint for Android app and browser listeners
checkoutRouter.get('/:id/stream', async (req: Request, res: Response) => {
  const orderId = req.params.id;
  const order = await db.getOrder(orderId);

  if (!order) {
    return res.status(404).json({ error: 'Order not found' });
  }

  // Setup SSE Headers
  res.setHeader('Content-Type', 'text/event-stream');
  res.setHeader('Cache-Control', 'no-cache');
  res.setHeader('Connection', 'keep-alive');
  res.setHeader('X-Accel-Buffering', 'no'); // Disable proxy buffering
  res.flushHeaders();

  // Register client
  if (!sseClients.has(orderId)) {
    sseClients.set(orderId, []);
  }
  sseClients.get(orderId)!.push(res);

  // Send initial event
  res.write(`data: ${JSON.stringify({
    type: 'CONNECTED',
    orderId,
    status: order.status,
    liveViewUrl: order.liveViewUrl || ''
  })}\n\n`);

  // Clean up on disconnect
  req.on('close', () => {
    const list = sseClients.get(orderId) || [];
    sseClients.set(orderId, list.filter(c => c !== res));
  });
});

// Start autonomous checkout process
checkoutRouter.post('/:id/start', async (req: Request, res: Response) => {
  try {
    const orderId = req.params.id;
    const order = await db.getOrder(orderId);

    if (!order) {
      return res.status(404).json({ error: 'Order not found' });
    }

    if (order.status !== 'AUTHORIZED') {
      return res.status(400).json({
        error: `Order must be AUTHORIZED before starting checkout. Current status: ${order.status}`
      });
    }

    // Acknowledge immediate start
    res.json({
      status: 'STARTING',
      orderId,
      message: 'Autonomous checkout agent initiated. Listen to /api/checkout/:id/stream for live updates.'
    });

    // Run agent in background, broadcasting every step via SSE and recording in database
    kernelAgent.runCheckout(orderId, async (event) => {
      console.log(`[Agent SSE] Order ${orderId} -> ${event.type} (${event.stepName || ''})`);
      broadcastEvent(orderId, event);

      if (event.type === 'STEP' && event.status === 'completed') {
        await db.addAuditLog({
          orderId,
          stepNumber: event.stepNumber || 0,
          stepName: event.stepName || 'Agent Action',
          description: event.description || '',
          status: 'COMPLETED'
        });
      }
    }).catch(err => {
      console.error(`[Checkout] Unhandled error during agent run for order ${orderId}:`, err);
    });

  } catch (error: any) {
    res.status(500).json({ error: error.message });
  }
});

// Get current checkout status and live view URL
checkoutRouter.get('/:id/status', async (req: Request, res: Response) => {
  try {
    const order = await db.getOrder(req.params.id);
    if (!order) {
      return res.status(404).json({ error: 'Order not found' });
    }
    const timeline = await db.getAuditLogs(order.id);
    res.json({
      orderId: order.id,
      status: order.status,
      liveViewUrl: order.liveViewUrl,
      merchantOrderId: order.merchantOrderId,
      paypalCaptureId: order.paypalCaptureId,
      failureReason: order.failureReason,
      timeline
    });
  } catch (error: any) {
    res.status(500).json({ error: error.message });
  }
});
