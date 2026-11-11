// Cartwise Web Application Server
// Node.js + Express Web Companion for Cartwise Autonomous AI Shopping Agent
// PayPal AI Hackathon 2026

const express = require('express');
const cors = require('cors');
const path = require('path');
const http = require('http');

const app = express();
const PORT = process.env.PORT || 5000;

// Resolve backend URL: In local preview/VM, prefer active local backend on port 3000.
// If process.env.BACKEND_URL is explicitly provided, use it. Otherwise, probe local backend.
const LOCAL_BACKEND = 'http://localhost:3000';
const DEFAULT_REMOTE_BACKEND = 'https://cartwise-backend-kr8p.onrender.com';
let cachedBackendUrl = process.env.BACKEND_URL ? process.env.BACKEND_URL.replace(/\/$/, '') : null;
let lastCheckTime = 0;

async function resolveBackendUrl() {
  if (process.env.BACKEND_URL) {
    return process.env.BACKEND_URL.replace(/\/$/, '');
  }

  const now = Date.now();
  // Cache the check for 10 seconds to avoid probing on every single request
  if (cachedBackendUrl && (now - lastCheckTime < 10000)) {
    return cachedBackendUrl;
  }

  try {
    const probe = await fetch(`${LOCAL_BACKEND}/health`, { signal: AbortSignal.timeout(800) });
    if (probe.ok) {
      cachedBackendUrl = LOCAL_BACKEND;
      lastCheckTime = now;
      return cachedBackendUrl;
    }
  } catch (err) {
    // Local backend not reachable
  }

  cachedBackendUrl = DEFAULT_REMOTE_BACKEND;
  lastCheckTime = now;
  return cachedBackendUrl;
}

app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Serve frontend assets
app.use(express.static(path.join(__dirname, 'public')));

// Health check
app.get('/health', async (req, res) => {
  const activeBackend = await resolveBackendUrl();
  res.json({
    status: 'ok',
    service: 'Cartwise Web Client',
    backendUrl: activeBackend,
    timestamp: new Date().toISOString()
  });
});

// Config endpoint for client
app.get('/api/web-config', async (req, res) => {
  const activeBackend = await resolveBackendUrl();
  res.json({
    backendUrl: activeBackend,
    paypalClientId: process.env.PAYPAL_CLIENT_ID || 'sandbox_client_id_placeholder',
    version: '1.0.0'
  });
});

// Proxy API requests to Cartwise Backend
app.all('/api/*', async (req, res) => {
  const activeBackend = await resolveBackendUrl();
  const targetPath = req.path;
  const targetUrl = `${activeBackend}${targetPath}${req.url.includes('?') ? req.url.slice(req.url.indexOf('?')) : ''}`;

  try {
    const clientHost = req.headers.host || `localhost:${PORT}`;
    const clientProto = req.headers['x-forwarded-proto'] || (req.secure ? 'https' : 'http');
    const clientOrigin = req.headers.origin || `${clientProto}://${clientHost}`;

    const fetchOptions = {
      method: req.method,
      headers: {
        'Content-Type': req.headers['content-type'] || 'application/json',
        'Accept': req.headers['accept'] || 'application/json',
        'Accept-Language': req.headers['accept-language'] || 'en-US,en;q=0.9,zh-CN,zh;q=0.8',
        'Origin': clientOrigin,
        'Referer': req.headers['referer'] || `${clientOrigin}/`,
        'User-Agent': req.headers['user-agent'] || 'Cartwise-Web-Client',
        'X-Forwarded-Host': clientHost,
        'X-Forwarded-Proto': clientProto
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
app.listen(PORT, async () => {
  const activeBackend = await resolveBackendUrl();
  console.log(`Cartwise Web running at http://localhost:${PORT}`);
  console.log(`Connected to Cartwise Backend: ${activeBackend}`);
});

module.exports = app;
