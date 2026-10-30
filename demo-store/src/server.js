const express = require('express');
const cors = require('cors');
const path = require('path');
const initialProducts = require('./products');
const { getProductSvg } = require('./icons');

const app = express();
const PORT = process.env.PORT || 4000;
const BASE_URL = process.env.DEMO_STORE_URL || process.env.RENDER_EXTERNAL_URL || `http://localhost:${PORT}`;

// In-memory state (resets on restart)
let products = JSON.parse(JSON.stringify(initialProducts));
let orders = [];

app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use('/public', express.static(path.join(__dirname, '../public')));

// Helper to calculate total for an item + shipping
function calculateTotal(product, quantity = 1, country = "NG") {
  const itemTotal = Number((product.price * quantity).toFixed(2));
  const shipping = Number(product.shippingCost.toFixed(2));
  return {
    itemTotal,
    shipping,
    total: Number((itemTotal + shipping).toFixed(2))
  };
}

// Common styles & HTML template wrapper
function renderPage(title, content) {
  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0">
  <title>${title} | Demo Market</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <style>
    :root {
      --primary: #4f46e5;
      --primary-dark: #4338ca;
      --accent: #06b6d4;
      --bg: #0b0f19;
      --card-bg: #111827;
      --card-border: #1f2937;
      --text: #f3f4f6;
      --text-muted: #9ca3af;
      --success: #10b981;
      --danger: #ef4444;
      --warning: #f59e0b;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
      background: var(--bg);
      color: var(--text);
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      -webkit-font-smoothing: antialiased;
    }
    .banner-warning {
      background: #2b1111;
      border-bottom: 1px solid #7f1d1d;
      color: #fca5a5;
      padding: 0.65rem 1rem;
      font-size: 0.8rem;
      text-align: center;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
      line-height: 1.4;
    }
    header {
      background: rgba(17, 24, 39, 0.92);
      backdrop-filter: blur(12px);
      border-bottom: 1px solid var(--card-border);
      position: sticky;
      top: 0;
      z-index: 50;
    }
    .header-inner {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0.9rem 1.25rem;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 0.75rem;
      flex-wrap: wrap;
    }
    .brand {
      display: flex;
      align-items: center;
      gap: 0.6rem;
      text-decoration: none;
      color: #fff;
      font-weight: 800;
      font-size: 1.15rem;
      letter-spacing: -0.025em;
    }
    .brand-badge {
      background: linear-gradient(135deg, #4f46e5, #06b6d4);
      color: #fff;
      font-size: 0.65rem;
      padding: 0.2rem 0.55rem;
      border-radius: 9999px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }
    .main-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 1.75rem 1.25rem;
      flex: 1;
      width: 100%;
    }
    .grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 1.25rem;
    }
    .card {
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-radius: 1rem;
      overflow: hidden;
      display: flex;
      flex-direction: column;
      transition: transform 0.2s, border-color 0.2s, box-shadow 0.2s;
    }
    .card:hover {
      transform: translateY(-3px);
      border-color: #374151;
      box-shadow: 0 12px 24px -10px rgba(0, 0, 0, 0.5);
    }
    .card-img-wrap {
      width: 100%;
      height: 200px;
      background: #0d121f;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 1rem;
      position: relative;
    }
    .card-body {
      padding: 1.15rem;
      display: flex;
      flex-direction: column;
      flex: 1;
    }
    .card-title {
      font-size: 1rem;
      font-weight: 700;
      color: #fff;
      margin-bottom: 0.5rem;
      line-height: 1.4;
    }
    .card-desc {
      font-size: 0.825rem;
      color: var(--text-muted);
      line-height: 1.5;
      margin-bottom: 1rem;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    .card-price-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-top: auto;
      padding-top: 0.85rem;
      border-top: 1px solid var(--card-border);
      gap: 0.75rem;
    }
    .price {
      font-size: 1.35rem;
      font-weight: 800;
      color: #fff;
    }
    .shipping-tag {
      font-size: 0.75rem;
      color: var(--accent);
      font-weight: 500;
    }
    .btn {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
      background: var(--primary);
      color: #fff;
      font-weight: 600;
      font-size: 0.9rem;
      padding: 0.75rem 1.25rem;
      border-radius: 0.6rem;
      border: none;
      cursor: pointer;
      text-decoration: none;
      transition: background 0.15s, transform 0.1s;
      min-height: 44px;
    }
    .btn:hover { background: var(--primary-dark); }
    .btn:active { transform: scale(0.98); }
    .btn-secondary {
      background: #1f2937;
      color: #e5e7eb;
      border: 1px solid #374151;
    }
    .btn-secondary:hover { background: #374151; }
    .badge {
      display: inline-block;
      font-size: 0.7rem;
      font-weight: 700;
      padding: 0.25rem 0.55rem;
      border-radius: 0.4rem;
      text-transform: uppercase;
      letter-spacing: 0.04em;
    }
    .badge-in-stock { background: #064e3b; color: #6ee7b7; border: 1px solid #047857; }
    .badge-out-stock { background: #7f1d1d; color: #fca5a5; border: 1px solid #b91c1c; }
    input, select {
      width: 100%;
      background: #1e293b;
      border: 1px solid #334155;
      color: #fff;
      padding: 0.75rem 1rem;
      border-radius: 0.5rem;
      font-size: 1rem; /* 16px prevents iOS zoom */
      margin-top: 0.35rem;
      margin-bottom: 0.9rem;
      outline: none;
      min-height: 46px;
    }
    input:focus, select:focus {
      border-color: var(--primary);
      box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.3);
    }
    label {
      font-size: 0.85rem;
      font-weight: 500;
      color: #d1d5db;
    }
    .form-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1rem;
    }

    /* Product Detail & Checkout Grids */
    .product-detail-grid {
      display: grid;
      grid-template-columns: minmax(280px, 1fr) minmax(320px, 1.25fr);
      gap: 2rem;
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-radius: 1.25rem;
      padding: 2rem;
    }
    .product-detail-image-box {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background: #0d121f;
      border-radius: 1rem;
      padding: 1.75rem;
      border: 1px solid #1f2937;
      width: 100%;
    }
    .checkout-grid {
      display: grid;
      grid-template-columns: 1.3fr 1fr;
      gap: 1.75rem;
    }

    /* Footer styling */
    footer.site-footer {
      border-top: 1px solid var(--card-border);
      background: #080c14;
      padding: 2.25rem 1.25rem 2rem;
      margin-top: auto;
    }
    .footer-inner {
      max-width: 800px;
      margin: 0 auto;
      text-align: center;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 1.25rem;
    }
    .footer-powered-wrap {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 0.65rem;
      color: var(--text-muted);
      font-size: 0.85rem;
    }
    .footer-powered-wrap span {
      letter-spacing: 0.02em;
    }
    .render-logo-img {
      height: 22px;
      width: auto;
      display: inline-block;
      vertical-align: middle;
      opacity: 0.95;
      transition: opacity 0.2s;
    }
    .render-logo-img:hover {
      opacity: 1;
    }
    .footer-bottom {
      border-top: 1px solid #1f2937;
      padding-top: 1rem;
      font-size: 0.75rem;
      color: #6b7280;
      width: 100%;
    }

    /* Mobile Responsive Overrides */
    @media (max-width: 768px) {
      .header-inner {
        padding: 0.75rem 1rem;
        flex-direction: row;
        justify-content: space-between;
      }
      .brand-subtext {
        display: none;
      }
      .main-container {
        padding: 1rem 0.85rem;
      }
      .catalog-header {
        flex-direction: column !important;
        align-items: flex-start !important;
        gap: 0.75rem !important;
      }
      .catalog-header h1 {
        font-size: 1.5rem !important;
      }
      .grid {
        grid-template-columns: 1fr;
        gap: 1rem;
      }
      .product-detail-grid {
        grid-template-columns: 1fr !important;
        gap: 1.25rem !important;
        padding: 1.15rem !important;
      }
      .product-detail-image-box {
        padding: 1.25rem !important;
      }
      .product-detail-image-box > div {
        width: 180px !important;
        height: 180px !important;
      }
      .checkout-grid {
        grid-template-columns: 1fr !important;
        gap: 1.25rem !important;
      }
      .form-row {
        grid-template-columns: 1fr !important;
        gap: 0.4rem !important;
      }
      .partner-grid {
        grid-template-columns: 1fr 1fr;
        gap: 0.75rem;
      }
      .partner-card {
        padding: 0.7rem 0.75rem;
      }
      .partner-role {
        font-size: 0.65rem;
      }
    }
    @media (max-width: 480px) {
      .partner-grid {
        grid-template-columns: 1fr;
      }
    }
  </style>
</head>
<body>
  <div class="banner-warning">
    <span>⚠️</span>
    <span><strong>Notice:</strong> Demo Market accepts <strong>Credit/Debit Cards ONLY</strong>. PayPal is <strong>NOT</strong> supported on this store.</span>
  </div>
  <header>
    <div class="header-inner">
      <a href="/" class="brand">
        <span>🛍️ Demo Market</span>
        <span class="brand-badge">Card Only</span>
      </a>
      <div class="brand-subtext" style="font-size: 0.825rem; color: #9ca3af;">
        Direct Mock Merchant for Cartwise AI
      </div>
    </div>
  </header>
  <main class="main-container">
    ${content}
  </main>
  <footer class="site-footer">
    <div class="footer-inner">
      <div class="footer-powered-wrap">
        <span>Hosted & Powered by</span>
        <a href="https://render.com" target="_blank" rel="noopener noreferrer" style="display: inline-flex; align-items: center; text-decoration: none;">
          <img src="/public/images/render-logo.png" alt="Render" class="render-logo-img" />
        </a>
      </div>

      <div class="footer-bottom">
        <p>Demo Market &bull; Simulated Card-Only Merchant for Cartwise AI Shopping Concierge</p>
        <p style="margin-top: 0.35rem;">PayPal AI Hackathon 2026 &bull; Lagos, Nigeria &bull; MIT License</p>
      </div>
    </div>
  </footer>
</body>
</html>`;
}

// -------------------------------------------------------------
// ROUTES
// -------------------------------------------------------------

// Health check
app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    store: 'Demo Market',
    timestamp: new Date().toISOString(),
    productsCount: products.length,
    inStockCount: products.filter(p => p.inStock).length
  });
});

// Catalog Homepage
app.get('/', (req, res) => {
  const productCards = products.map(p => `
    <div class="card" id="product-${p.id}">
      <div class="card-img-wrap">
        ${getProductSvg(p.iconType)}
        <div style="position: absolute; top: 0.75rem; right: 0.75rem;">
          <span class="badge ${p.inStock ? 'badge-in-stock' : 'badge-out-stock'}">
            ${p.inStock ? 'In Stock' : 'Out of Stock'}
          </span>
        </div>
      </div>
      <div class="card-body">
        <div style="font-size: 0.75rem; color: var(--accent); font-weight: 600; text-transform: uppercase; margin-bottom: 0.25rem;">
          ${p.brand} &bull; ${p.category}
        </div>
        <h2 class="card-title">${p.title}</h2>
        <p class="card-desc">${p.description}</p>
        <div class="card-price-row">
          <div>
            <div class="price">$${p.price.toFixed(2)}</div>
            <div class="shipping-tag">+$${p.shippingCost.toFixed(2)} shipping</div>
          </div>
          <a href="/product/${p.id}" class="btn" style="padding: 0.5rem 1rem; font-size: 0.85rem;">
            View Item
          </a>
        </div>
      </div>
    </div>
  `).join('');

  const content = `
    <div class="catalog-header" style="margin-bottom: 2rem; display: flex; justify-content: space-between; align-items: flex-end; flex-wrap: wrap; gap: 1rem;">
      <div>
        <h1 style="font-size: 1.85rem; font-weight: 800; color: #fff; margin-bottom: 0.5rem;">Explore All Products</h1>
        <p style="color: var(--text-muted); font-size: 0.95rem;">
          Curated electronics, audio, and gadgets. All products ship globally including Lagos, Nigeria.
        </p>
      </div>
      <div style="background: #1e293b; padding: 0.6rem 1rem; border-radius: 0.5rem; font-size: 0.8rem; border: 1px solid #334155; white-space: nowrap;">
        💳 <strong>Card Form Only</strong> &bull; No PayPal Checkout
      </div>
    </div>
    <div class="grid">
      ${productCards}
    </div>
  `;

  res.send(renderPage('Home - Shop Gadgets & Tech', content));
});

// Product Detail Page
app.get('/product/:id', (req, res) => {
  const product = products.find(p => p.id === req.params.id);
  if (!product) {
    return res.status(404).send(renderPage('Not Found', '<h1>Product Not Found</h1><a href="/" class="btn" style="margin-top:1rem;">Back to Store</a>'));
  }

  const variantOptions = product.variants.map((v, i) => `
    <option value="${v.sku}" ${i === 0 ? 'selected' : ''}>${v.name} (${v.sku})</option>
  `).join('');

  const featuresList = product.features.map(f => `
    <li style="margin-bottom: 0.5rem; display: flex; align-items: center; gap: 0.5rem;">
      <span style="color: var(--success);">✓</span> ${f}
    </li>
  `).join('');

  const content = `
    <div style="margin-bottom: 1.5rem;">
      <a href="/" style="color: var(--text-muted); text-decoration: none; font-size: 0.85rem; display: inline-flex; align-items: center; gap: 0.35rem;">
        <span>←</span> <span>Back to All Products</span>
      </a>
    </div>
    <div class="product-detail-grid">
      <!-- Product Image -->
      <div class="product-detail-image-box">
        <div style="width: 220px; height: 220px; display: flex; align-items: center; justify-content: center;">
          ${getProductSvg(product.iconType)}
        </div>
        <div style="margin-top: 1.25rem; display: flex; gap: 0.5rem; flex-wrap: wrap; justify-content: center;">
          <span class="badge ${product.inStock ? 'badge-in-stock' : 'badge-out-stock'}">
            ${product.inStock ? `In Stock (${product.stockCount} left)` : 'Out of Stock'}
          </span>
          <span class="badge" style="background: #1e293b; color: #94a3b8; border: 1px solid #334155;">SKU: ${product.variants[0].sku}</span>
        </div>
      </div>

      <!-- Product Info & Purchase Form -->
      <div>
        <div style="font-size: 0.8rem; color: var(--accent); font-weight: 700; text-transform: uppercase;">
          ${product.brand} &bull; ${product.category}
        </div>
        <h1 style="font-size: 1.75rem; font-weight: 800; color: #fff; margin: 0.5rem 0 0.75rem; line-height: 1.3;">
          ${product.title}
        </h1>
        <div style="display: flex; align-items: baseline; gap: 0.75rem; margin-bottom: 1.25rem; flex-wrap: wrap;">
          <div style="font-size: 2rem; font-weight: 800; color: #fff;">$${product.price.toFixed(2)}</div>
          <div style="font-size: 0.9rem; color: var(--accent); font-weight: 600;">
            + $${product.shippingCost.toFixed(2)} shipping to Lagos, NG
          </div>
        </div>

        <p style="color: #d1d5db; line-height: 1.6; margin-bottom: 1.25rem; font-size: 0.925rem;">
          ${product.description}
        </p>

        <ul style="list-style: none; margin-bottom: 1.5rem; font-size: 0.875rem; color: #9ca3af;">
          ${featuresList}
        </ul>

        <form action="/checkout" method="GET" style="border-top: 1px solid var(--card-border); padding-top: 1.25rem;">
          <input type="hidden" name="productId" value="${product.id}">
          
          <div class="form-row">
            <div>
              <label for="variantSelect">Choose Variant</label>
              <select id="variantSelect" name="variant">
                ${variantOptions}
              </select>
            </div>
            <div>
              <label for="qtySelect">Quantity</label>
              <select id="qtySelect" name="quantity">
                <option value="1">1</option>
                <option value="2">2</option>
                <option value="3">3</option>
              </select>
            </div>
          </div>

          ${product.inStock ? `
            <button type="submit" class="btn" id="buyNowBtn" style="width: 100%; padding: 0.9rem; font-size: 1.05rem; margin-top: 0.5rem;">
              Proceed to Checkout ($${(product.price + product.shippingCost).toFixed(2)})
            </button>
          ` : `
            <div style="background: #311313; border: 1px solid #7f1d1d; color: #fca5a5; padding: 1rem; border-radius: 0.5rem; text-align: center; font-weight: 600; margin-top: 0.5rem;">
              ⚠️ This item is currently Out of Stock.
            </div>
          `}
        </form>

        <!-- Admin toggle for hackathon live demo failure simulation -->
        <div style="margin-top: 1.5rem; padding: 0.85rem 1rem; background: #0f172a; border-radius: 0.5rem; border: 1px dashed #334155; font-size: 0.75rem;">
          <div style="color: #eab308; font-weight: 700; margin-bottom: 0.35rem;">Hackathon Test Control:</div>
          <button onclick="toggleStock('${product.id}')" style="background: #374151; color: #fff; border: 1px solid #4b5563; padding: 0.45rem 0.85rem; border-radius: 0.35rem; cursor: pointer; font-size: 0.8rem; font-weight: 600;">
            Toggle Stock Status (Simulate Stockout)
          </button>
        </div>
      </div>
    </div>
    <script>
      async function toggleStock(id) {
        const res = await fetch('/api/products/' + id + '/toggle-stock', { method: 'POST' });
        const data = await res.json();
        alert('Stock status changed to: ' + (data.inStock ? 'IN STOCK' : 'OUT OF STOCK'));
        location.reload();
      }
    </script>
  `;

  res.send(renderPage(product.title, content));
});

// Checkout Page (Where the Cartwise AI Agent navigates to input shipping & card)
app.get('/checkout', (req, res) => {
  const productId = req.query.productId || products[0].id;
  const product = products.find(p => p.id === productId);

  if (!product) {
    return res.redirect('/');
  }

  const quantity = parseInt(req.query.quantity) || 1;
  const variant = req.query.variant || product.variants[0].sku;
  const { itemTotal, shipping, total } = calculateTotal(product, quantity);

  const content = `
    <div style="max-width: 900px; margin: 0 auto;">
      <h1 style="font-size: 1.85rem; font-weight: 800; color: #fff; margin-bottom: 0.35rem;">Secure Checkout</h1>
      <p style="color: var(--text-muted); margin-bottom: 1.75rem; font-size: 0.9rem;">Demo Market Store &bull; Standard Credit/Debit Card Order</p>

      ${!product.inStock ? `
        <div style="background: #311313; border: 1px solid #7f1d1d; color: #fca5a5; padding: 1.25rem; border-radius: 0.75rem; margin-bottom: 1.5rem; text-align: center;">
          <h3 style="margin-bottom: 0.35rem;">⚠️ Cannot Proceed: Item Out of Stock</h3>
          <p>The product <strong>${product.title}</strong> has sold out.</p>
        </div>
      ` : ''}

      <div class="checkout-grid">
        <!-- Checkout Form -->
        <div style="background: var(--card-bg); border: 1px solid var(--card-border); border-radius: 1rem; padding: 1.75rem;">
          <form id="checkoutForm" action="/api/checkout" method="POST">
            <input type="hidden" name="productId" value="${product.id}">
            <input type="hidden" name="quantity" value="${quantity}">
            <input type="hidden" name="variant" value="${variant}">

            <h3 style="font-size: 1.1rem; font-weight: 700; color: #fff; margin-bottom: 1rem; padding-bottom: 0.5rem; border-bottom: 1px solid var(--card-border);">
              1. Shipping Address
            </h3>

            <label for="fullName">Full Name</label>
            <input type="text" id="fullName" name="fullName" placeholder="e.g. Ada Lovelace" required value="Ada Lovelace">

            <label for="email">Email Address</label>
            <input type="email" id="email" name="email" placeholder="e.g. ada@example.com" required value="ada@cartwise.shopper">

            <label for="address">Street Address</label>
            <input type="text" id="address" name="address" placeholder="e.g. 14 Marina Street, Lagos Island" required value="14 Marina Street">

            <div class="form-row">
              <div>
                <label for="city">City</label>
                <input type="text" id="city" name="city" placeholder="Lagos" required value="Lagos">
              </div>
              <div>
                <label for="state">State / Province</label>
                <input type="text" id="state" name="state" placeholder="Lagos State" required value="Lagos">
              </div>
            </div>

            <div class="form-row">
              <div>
                <label for="country">Country</label>
                <select id="country" name="country" required>
                  <option value="NG" selected>Nigeria (NG)</option>
                  <option value="US">United States (US)</option>
                  <option value="UK">United Kingdom (UK)</option>
                  <option value="CA">Canada (CA)</option>
                  <option value="GH">Ghana (GH)</option>
                </select>
              </div>
              <div>
                <label for="postalCode">Postal Code</label>
                <input type="text" id="postalCode" name="postalCode" placeholder="100001" value="100001">
              </div>
            </div>

            <h3 style="font-size: 1.1rem; font-weight: 700; color: #fff; margin: 1.5rem 0 1rem; padding-bottom: 0.5rem; border-bottom: 1px solid var(--card-border);">
              2. Payment Information
            </h3>

            <div style="background: #172554; border: 1px solid #1e40af; color: #93c5fd; padding: 0.75rem 1rem; border-radius: 0.5rem; font-size: 0.8rem; margin-bottom: 1rem;">
              💳 <strong>Credit or Debit Card</strong> (Demo Card: 4242 4242 4242 4242)
            </div>

            <label for="cardName">Cardholder Name</label>
            <input type="text" id="cardName" name="cardName" placeholder="Name on card" required value="Cartwise Commercial Card">

            <label for="cardNumber">Card Number</label>
            <input type="text" id="cardNumber" name="cardNumber" placeholder="4242 4242 4242 4242" maxlength="19" required autocomplete="cc-number">

            <div class="form-row">
              <div>
                <label for="cardExpiry">Expiration (MM/YY)</label>
                <input type="text" id="cardExpiry" name="cardExpiry" placeholder="12/28" maxlength="5" required autocomplete="cc-exp">
              </div>
              <div>
                <label for="cardCvc">CVC Code</label>
                <input type="text" id="cardCvc" name="cardCvc" placeholder="123" maxlength="4" required autocomplete="cc-csc">
              </div>
            </div>

            <div id="checkoutError" style="display: none; background: #311313; border: 1px solid #7f1d1d; color: #fca5a5; padding: 0.75rem; border-radius: 0.5rem; font-size: 0.85rem; margin-bottom: 1rem;"></div>

            <button type="submit" id="placeOrderBtn" class="btn" style="width: 100%; padding: 1rem; font-size: 1.05rem;" ${!product.inStock ? 'disabled style="opacity:0.5; cursor:not-allowed;"' : ''}>
              Place Order ($${total.toFixed(2)})
            </button>
          </form>
        </div>

        <!-- Order Summary Sidebar -->
        <div>
          <div style="background: var(--card-bg); border: 1px solid var(--card-border); border-radius: 1rem; padding: 1.5rem; position: sticky; top: 90px;">
            <h3 style="font-size: 1.1rem; font-weight: 700; color: #fff; margin-bottom: 1.25rem;">
              Order Summary
            </h3>

            <div style="display: flex; gap: 1rem; padding-bottom: 1.25rem; border-bottom: 1px solid var(--card-border); margin-bottom: 1.25rem;">
              <div style="width: 60px; height: 60px; background: #0d121f; border-radius: 0.5rem; padding: 0.35rem;">
                ${getProductSvg(product.iconType)}
              </div>
              <div>
                <div style="font-size: 0.9rem; font-weight: 600; color: #fff;">${product.title}</div>
                <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.25rem;">Variant: ${variant}</div>
                <div style="font-size: 0.8rem; color: var(--text-muted);">Qty: ${quantity} &times; $${product.price.toFixed(2)}</div>
              </div>
            </div>

            <div style="font-size: 0.9rem; color: #d1d5db; display: flex; justify-content: space-between; margin-bottom: 0.75rem;">
              <span>Subtotal</span>
              <span id="summarySubtotal">$${itemTotal.toFixed(2)}</span>
            </div>
            <div style="font-size: 0.9rem; color: #d1d5db; display: flex; justify-content: space-between; margin-bottom: 0.75rem;">
              <span>Shipping to Lagos</span>
              <span id="summaryShipping">$${shipping.toFixed(2)}</span>
            </div>
            <div style="font-size: 0.9rem; color: #d1d5db; display: flex; justify-content: space-between; margin-bottom: 1.25rem;">
              <span>Estimated Tax</span>
              <span>$0.00</span>
            </div>

            <div style="display: flex; justify-content: space-between; align-items: baseline; padding-top: 1.25rem; border-top: 1px solid var(--card-border); font-size: 1.2rem; font-weight: 800; color: #fff;">
              <span>Total</span>
              <span id="summaryTotal">$${total.toFixed(2)}</span>
            </div>

            <div style="margin-top: 1.5rem; background: #0f172a; padding: 0.75rem; border-radius: 0.5rem; font-size: 0.75rem; color: #94a3b8; text-align: center;">
              🔒 256-Bit SSL Encrypted Mock Merchant Checkout
            </div>
          </div>
        </div>
      </div>
    </div>

    <script>
      const form = document.getElementById('checkoutForm');
      const errorDiv = document.getElementById('checkoutError');

      form.addEventListener('submit', async (e) => {
        e.preventDefault();
        errorDiv.style.display = 'none';
        const submitBtn = document.getElementById('placeOrderBtn');
        submitBtn.disabled = true;
        submitBtn.innerText = 'Processing Order...';

        const formData = new FormData(form);
        const data = Object.fromEntries(formData.entries());

        try {
          const res = await fetch('/api/checkout', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
          });
          const result = await res.json();
          if (result.success) {
            window.location.href = '/order-confirmation?orderId=' + result.orderId;
          } else {
            errorDiv.innerText = 'Checkout Failed: ' + (result.error || 'Unknown error');
            errorDiv.style.display = 'block';
            submitBtn.disabled = false;
            submitBtn.innerText = 'Place Order';
          }
        } catch (err) {
          errorDiv.innerText = 'Network error: ' + err.message;
          errorDiv.style.display = 'block';
          submitBtn.disabled = false;
          submitBtn.innerText = 'Place Order';
        }
      });
    </script>
  `;

  res.send(renderPage('Checkout', content));
});

// Order Confirmation Page
app.get('/order-confirmation', (req, res) => {
  const orderId = req.query.orderId;
  const order = orders.find(o => o.orderId === orderId);

  if (!order) {
    return res.send(renderPage('Order Not Found', '<h1>Order Not Found</h1><a href="/" class="btn" style="margin-top:1rem;">Back to Store</a>'));
  }

  const content = `
    <div style="max-width: 650px; margin: 2rem auto; background: var(--card-bg); border: 1px solid var(--card-border); border-radius: 1.25rem; padding: 2.5rem; text-align: center;">
      <div style="width: 72px; height: 72px; background: #064e3b; color: #34d399; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 2.25rem; margin: 0 auto 1.5rem;">
        ✓
      </div>
      <h1 style="font-size: 1.85rem; font-weight: 800; color: #fff; margin-bottom: 0.5rem;">Order Confirmed!</h1>
      <p style="color: var(--text-muted); font-size: 0.95rem; margin-bottom: 2rem;">
        Thank you for your purchase. We have received your card payment and your order is being prepared.
      </p>

      <div style="background: #0f172a; border: 1px solid #1f2937; border-radius: 0.75rem; padding: 1.5rem; text-align: left; margin-bottom: 2rem;">
        <div style="display: flex; justify-content: space-between; border-bottom: 1px solid #1f2937; padding-bottom: 0.75rem; margin-bottom: 0.75rem;">
          <span style="color: var(--text-muted);">Merchant Order ID:</span>
          <span id="confirmationOrderId" style="color: #fff; font-weight: 700; font-family: monospace;">${order.orderId}</span>
        </div>
        <div style="display: flex; justify-content: space-between; border-bottom: 1px solid #1f2937; padding-bottom: 0.75rem; margin-bottom: 0.75rem;">
          <span style="color: var(--text-muted);">Item Purchased:</span>
          <span style="color: #fff; font-weight: 600;">${order.productTitle}</span>
        </div>
        <div style="display: flex; justify-content: space-between; border-bottom: 1px solid #1f2937; padding-bottom: 0.75rem; margin-bottom: 0.75rem;">
          <span style="color: var(--text-muted);">Shipping Address:</span>
          <span style="color: #fff; text-align: right;">${order.shipping.address}, ${order.shipping.city}, ${order.shipping.country}</span>
        </div>
        <div style="display: flex; justify-content: space-between; border-bottom: 1px solid #1f2937; padding-bottom: 0.75rem; margin-bottom: 0.75rem;">
          <span style="color: var(--text-muted);">Payment Method:</span>
          <span style="color: #fff;">Card ending in <strong>${order.cardLast4}</strong></span>
        </div>
        <div style="display: flex; justify-content: space-between; font-size: 1.15rem; font-weight: 800; color: #fff; padding-top: 0.5rem;">
          <span>Total Charged:</span>
          <span id="confirmationTotal">$${order.total.toFixed(2)}</span>
        </div>
      </div>

      <div style="display: flex; justify-content: center; gap: 1rem;">
        <a href="/" class="btn btn-secondary">Shop More Items</a>
      </div>
    </div>
  `;

  res.send(renderPage('Order Confirmed #' + order.orderId, content));
});

// -------------------------------------------------------------
// JSON API ENDPOINTS (For Cartwise Backend Integration)
// -------------------------------------------------------------

// Get all products (Formatted identically to Channel3 products structure for seamless merge)
app.get('/api/products', (req, res) => {
  const host = req.get('host');
  const protocol = req.protocol;
  const currentBase = `${protocol}://${host}`;

  const formatted = products.map(p => ({
    id: p.id,
    title: p.title,
    description: p.description,
    category: p.category,
    brand: {
      id: `brand_${p.brand.toLowerCase()}`,
      name: p.brand
    },
    images: [
      `${currentBase}/api/products/${p.id}/image.svg`
    ],
    offers: [
      {
        website_id: "demo_market",
        website_name: "Demo Market",
        price: p.price,
        currency: p.currency,
        availability: p.inStock ? "InStock" : "OutOfStock",
        shippingCost: p.shippingCost,
        shipsTo: p.shipsTo,
        url: `${currentBase}/product/${p.id}`
      }
    ],
    metadata: {
      rating: p.rating,
      reviewsCount: p.reviewsCount,
      stockCount: p.stockCount,
      inStock: p.inStock,
      shipsToLagos: p.shipsTo.includes("NG")
    }
  }));

  res.json({
    products: formatted,
    total: formatted.length,
    store: "Demo Market"
  });
});

// Serve direct SVG image for a product
app.get('/api/products/:id/image.svg', (req, res) => {
  const product = products.find(p => p.id === req.params.id);
  if (!product) {
    return res.status(404).send('Image not found');
  }
  res.setHeader('Content-Type', 'image/svg+xml');
  res.send(getProductSvg(product.iconType));
});

// Get single product details
app.get('/api/products/:id', (req, res) => {
  const product = products.find(p => p.id === req.params.id);
  if (!product) {
    return res.status(404).json({ error: 'Product not found' });
  }
  res.json(product);
});

// Toggle stock status (for testing out-of-stock failure flow)
app.post('/api/products/:id/toggle-stock', (req, res) => {
  const product = products.find(p => p.id === req.params.id);
  if (!product) {
    return res.status(404).json({ error: 'Product not found' });
  }
  product.inStock = !product.inStock;
  product.stockCount = product.inStock ? 15 : 0;
  res.json({
    id: product.id,
    title: product.title,
    inStock: product.inStock,
    stockCount: product.stockCount
  });
});

// Checkout API (Used by autonomous Playwright agent or frontend form)
app.post('/api/checkout', (req, res) => {
  const { productId, quantity = 1, variant, fullName, email, address, city, state, country, postalCode, cardNumber, cardExpiry, cardCvc, cardName } = req.body;

  const product = products.find(p => p.id === productId);
  if (!product) {
    return res.status(400).json({ success: false, error: 'Invalid product ID' });
  }

  // Stock check guardrail (Failure simulation)
  if (!product.inStock || product.stockCount <= 0) {
    return res.status(400).json({
      success: false,
      error: 'Product is OUT OF STOCK. Order cannot be placed.',
      errorCode: 'OUT_OF_STOCK'
    });
  }

  // Basic card validation
  const cleanCard = (cardNumber || '').replace(/\s+/g, '');
  if (!cleanCard || cleanCard.length < 13) {
    return res.status(400).json({ success: false, error: 'Invalid credit card number' });
  }

  const { itemTotal, shipping, total } = calculateTotal(product, parseInt(quantity));

  // Generate unique merchant order ID
  const orderId = `DM-${Math.floor(100000 + Math.random() * 900000)}`;
  const newOrder = {
    orderId,
    productId: product.id,
    productTitle: product.title,
    quantity: parseInt(quantity),
    variant: variant || product.variants[0].sku,
    total,
    itemTotal,
    shippingCost: shipping,
    shipping: {
      fullName,
      email,
      address,
      city,
      state,
      country: country || 'NG',
      postalCode
    },
    cardLast4: cleanCard.slice(-4),
    status: 'CONFIRMED',
    createdAt: new Date().toISOString()
  };

  orders.push(newOrder);

  // Decrement stock
  product.stockCount -= parseInt(quantity);
  if (product.stockCount <= 0) {
    product.inStock = false;
  }

  res.json({
    success: true,
    orderId,
    total,
    currency: 'USD',
    merchant: 'Demo Market',
    status: 'CONFIRMED'
  });
});

// Order lookup
app.get('/api/orders/:orderId', (req, res) => {
  const order = orders.find(o => o.orderId === req.params.orderId);
  if (!order) {
    return res.status(404).json({ error: 'Order not found' });
  }
  res.json(order);
});

// Start server
if (require.main === module) {
  app.listen(PORT, () => {
    console.log(`Demo Market running at http://localhost:${PORT}`);
  });
}

module.exports = app;
