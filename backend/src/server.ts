// Cartwise Backend API Server
// PayPal AI Hackathon 2026 Submission

import express from 'express';
import cors from 'cors';
import { config } from './config';
import { ordersRouter } from './routes/orders';
import { webhooksRouter } from './routes/webhooks';
import { searchRouter } from './routes/search';
import { checkoutRouter } from './routes/checkout';

const app = express();

app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Health Check
app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    service: 'Cartwise AI Agent Backend',
    version: '1.0.0',
    paypalConfigured: config.paypal.clientId !== 'sandbox_client_id_placeholder',
    geminiConfigured: Boolean(config.gemini.apiKey),
    channel3Configured: Boolean(config.channel3.apiKey),
    kernelConfigured: Boolean(config.kernel.apiKey),
    demoMode: config.demoMode,
    timestamp: new Date().toISOString()
  });
});

// Mount Routes
app.use('/api/search', searchRouter);
app.use('/api/orders', ordersRouter);
app.use('/api/checkout', checkoutRouter);
app.use('/api/webhooks', webhooksRouter);

// Global Error Handler
app.use((err: any, req: express.Request, res: express.Response, next: express.NextFunction) => {
  console.error('[Unhandled Error]', err);
  res.status(500).json({
    error: err.message || 'Internal Server Error',
    code: err.code || 'INTERNAL_ERROR'
  });
});

// Start listening if run directly
if (require.main === module) {
  app.listen(config.port, () => {
    console.log(` Cartwise Backend running at ${config.backendUrl}`);
    console.log(` Demo Store Target: ${config.demoStoreUrl}`);
    console.log(` PayPal Sandbox: ${config.paypal.apiUrl}`);
  });
}

export default app;
