// Cartwise Web Application Server
// Node.js + Express Web Companion for Cartwise Autonomous AI Shopping Agent
// PayPal AI Hackathon 2026

const express = require('express');
const cors = require('cors');
const path = require('path');
const http = require('http');

const app = express();
const PORT = process.env.PORT || 5000;
const BACKEND_URL = (process.env.BACKEND_URL || 'https://cartwise-backend-kr8p.onrender.com').replace(/\/$/, '');

app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Serve frontend assets
app.use(express.static(path.join(__dirname, 'public')));

// Health check
app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    service: 'Cartwise Web Client',
    backendUrl: BACKEND_URL,
    timestamp: new Date().toISOString()
  });
});

// Config endpoint for client
app.get('/api/web-config', (req, res) => {
  res.json({
    backendUrl: BACKEND_URL,
    paypalClientId: process.env.PAYPAL_CLIENT_ID || 'sandbox_client_id_placeholder',
    version: '1.0.0'
  });
});

// Proxy API requests to Cartwise Backend
app.all('/api/*', async (req, res) => {
  const targetPath = req.path;
  const targetUrl = `${BACKEND_URL}${targetPath}${req.url.includes('?') ? req.url.slice(req.url.indexOf('?')) : ''}`;

  try {
    const fetchOptions = {
      method: req.method,
      headers: {
        'Content-Type': req.headers['content-type'] || 'application/json',
        'Accept': req.headers['accept'] || 'application/json',
        'Accept-Language': req.headers['accept-language'] || 'en-US,en;q=0.9,zh-CN,zh;q=0.8'
      }
    };

    if (['POST', 'PUT', 'PATCH'].includes(req.method) && req.body && Object.keys(req.body).length > 0) {
      fetchOptions.body = JSON.stringify(req.body);
    }

    const backendRes = await fetch(targetUrl, fetchOptions);

    // Forward SSE streams
    if (backendRes.headers.get('content-type')?.includes('text/event-stream')) {
      res.setHeader('Content-Type', 'text/event-stream');
      res.setHeader('Cache-Control', 'no-cache');
      res.setHeader('Connection', 'keep-alive');
      
      const reader = backendRes.body.getReader();
      const decoder = new TextDecoder();
      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        res.write(decoder.decode(value, { stream: true }));
      }
      return res.end();
    }

    const data = await backendRes.text();
    res.status(backendRes.status);
    const contentType = backendRes.headers.get('content-type');
    if (contentType) res.setHeader('Content-Type', contentType);
    res.send(data);
  } catch (err) {
    console.error(`[Proxy Error] Failed to proxy ${req.method} ${targetUrl}:`, err.message);
    res.status(502).json({
      error: 'Failed to communicate with Cartwise backend',
      details: err.message,
      targetUrl
    });
  }
});

// SPA fallback to index.html
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

// Start server
app.listen(PORT, () => {
  console.log(`Cartwise Web running at http://localhost:${PORT}`);
  console.log(`Connected to Cartwise Backend: ${BACKEND_URL}`);
});

module.exports = app;
