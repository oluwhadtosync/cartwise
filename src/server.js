const express = require('express');
const cors = require('cors');
const path = require('path');
const initialProducts = require('./products');

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

// Helper to escape HTML characters safely
function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

// Common styles & HTML template wrapper (Clean Modern White Theme)
function renderPage(title, content) {
  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0">
  <title>${title} | Demo Market</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
  <style>
    :root {
      --primary: #f97316;
      --primary-dark: #ea580c;
      --primary-gradient: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
      --dark-btn: #0f172a;
      --dark-btn-hover: #1e293b;
      --bg: #f8fafc;
      --card-bg: #ffffff;
      --card-border: #e2e8f0;
      --text: #0f172a;
      --text-muted: #64748b;
      --accent-blue: #0369a1;
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
      background: #fff7ed;
      border-bottom: 1px solid #fed7aa;
      color: #9a3412;
      padding: 0.65rem 1rem;
      font-size: 0.825rem;
      text-align: center;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
      line-height: 1.4;
      font-weight: 500;
    }
    header {
      background: rgba(255, 255, 255, 0.95);
      backdrop-filter: blur(12px);
      border-bottom: 1px solid var(--card-border);
      position: sticky;
      top: 0;
      z-index: 50;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
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
      display: inline-flex;
      align-items: center;
      gap: 0.6rem;
      text-decoration: none;
      color: #0f172a;
      font-weight: 800;
      font-size: 1.25rem;
      letter-spacing: -0.025em;
      min-height: 44px;
    }
    .brand-badge {
      background: var(--primary-gradient);
      color: #fff;
      font-size: 0.75rem;
      padding: 0.25rem 0.6rem;
      border-radius: 9999px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      box-shadow: 0 2px 6px rgba(249, 115, 22, 0.25);
    }
    .brand-subtext {
      font-size: 0.825rem;
      color: var(--text-muted);
      font-weight: 500;
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
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05), 0 1px 2px rgba(0, 0, 0, 0.03);
      transition: transform 0.2s, border-color 0.2s, box-shadow 0.2s;
    }
    .card:hover {
      transform: translateY(-3px);
      border-color: #cbd5e1;
      box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.08), 0 8px 10px -6px rgba(0, 0, 0, 0.04);
    }
    .card-img-wrap {
      width: 100%;
      height: 210px;
      background: #f8fafc;
      border-bottom: 1px solid #f1f5f9;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 1.25rem;
      position: relative;
      overflow: hidden;
    }
    .card-img {
      max-width: 100%;
      max-height: 100%;
      object-fit: contain;
      transition: transform 0.3s ease;
    }
    .card:hover .card-img {
      transform: scale(1.05);
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
      color: #0f172a;
      margin-bottom: 0.45rem;
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
      border-top: 1px solid #f1f5f9;
      gap: 0.5rem;
    }
    .price {
      font-size: 1.35rem;
      font-weight: 800;
      color: #0f172a;
    }
    .shipping-tag {
      font-size: 0.75rem;
      color: #0369a1;
      font-weight: 700;
    }
    .card-actions {
      display: flex;
      align-items: center;
      gap: 0.4rem;
    }
    .btn {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 0.45rem;
      font-weight: 600;
      font-size: 0.875rem;
      padding: 0.65rem 1.15rem;
      border-radius: 0.6rem;
      border: none;
      cursor: pointer;
      text-decoration: none;
      transition: all 0.15s ease;
      min-height: 44px;
    }
    .btn:active { transform: scale(0.98); }
    /* Orange gradient primary button */
    .btn-orange, .btn-primary {
      background: var(--primary-gradient);
      color: #ffffff !important;
      font-weight: 700;
      box-shadow: 0 4px 12px rgba(249, 115, 22, 0.3);
    }
    .btn-orange:hover, .btn-primary:hover {
      background: linear-gradient(135deg, #ea580c 0%, #c2410c 100%);
      box-shadow: 0 6px 16px rgba(249, 115, 22, 0.4);
      transform: translateY(-1px);
    }
    /* Dark secondary button */
    .btn-dark {
      background: var(--dark-btn);
      color: #ffffff !important;
      border: 1px solid #1e293b;
      box-shadow: 0 2px 4px rgba(15, 23, 42, 0.1);
    }
    .btn-dark:hover {
      background: var(--dark-btn-hover);
      border-color: #334155;
      transform: translateY(-1px);
    }
    /* Clean Share Button */
    .btn-share {
      background: #f1f5f9;
      color: #1e293b;
      border: 1px solid #cbd5e1;
      padding: 0.45rem 0.75rem;
      font-size: 0.825rem;
      font-weight: 600;
      min-height: 44px;
    }
    .btn-share:hover {
      background: #e2e8f0;
      color: #0f172a;
      border-color: #94a3b8;
      transform: translateY(-1px);
    }
    .badge {
      display: inline-block;
      font-size: 0.75rem;
      font-weight: 700;
      padding: 0.3rem 0.65rem;
      border-radius: 0.4rem;
      text-transform: uppercase;
      letter-spacing: 0.04em;
    }
    .badge-in-stock { background: #ecfdf5; color: #065f46; border: 1px solid #a7f3d0; }
    .badge-out-stock { background: #fef2f2; color: #991b1b; border: 1px solid #fecaca; }
    input, select {
      width: 100%;
      background: #ffffff;
      border: 1px solid #cbd5e1;
      color: #0f172a;
      padding: 0.75rem 1rem;
      border-radius: 0.5rem;
      font-size: 1rem;
      margin-top: 0.35rem;
      margin-bottom: 0.9rem;
      outline: none;
      min-height: 46px;
      transition: border-color 0.15s, box-shadow 0.15s;
    }
    input:focus, select:focus {
      border-color: #f97316;
      box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.15);
    }
    label {
      font-size: 0.85rem;
      font-weight: 600;
      color: #334155;
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
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.04);
    }
    .product-detail-image-box {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background: #f8fafc;
      border-radius: 1rem;
      padding: 1.75rem;
      border: 1px solid #e2e8f0;
      width: 100%;
    }
    .product-detail-img {
      max-width: 100%;
      max-height: 320px;
      object-fit: contain;
      border-radius: 0.5rem;
    }
    .checkout-grid {
      display: grid;
      grid-template-columns: 1.3fr 1fr;
      gap: 1.75rem;
    }

    /* Toast notification */
    #shareToast {
      position: fixed;
      bottom: 1.5rem;
      right: 1.5rem;
      background: #0f172a;
      color: #fff;
      padding: 0.85rem 1.25rem;
      border-radius: 0.75rem;
      box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
      font-size: 0.875rem;
      font-weight: 500;
      z-index: 100;
      display: none;
      align-items: center;
      gap: 0.5rem;
      border: 1px solid #334155;
    }

    /* Search and Filter Section */
    .search-filter-section {
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-radius: 1rem;
      padding: 1.15rem;
      margin-bottom: 1.75rem;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
      display: flex;
      flex-direction: column;
      gap: 0.9rem;
    }
    .search-input-wrap {
      position: relative;
      width: 100%;
      display: flex;
      align-items: center;
    }
    .search-icon {
      position: absolute;
      left: 1rem;
      color: #94a3b8;
      pointer-events: none;
      width: 18px;
      height: 18px;
    }
    .search-input {
      width: 100%;
      background: #f8fafc;
      border: 1.5px solid #cbd5e1;
      border-radius: 0.75rem;
      padding: 0.7rem 2.5rem 0.7rem 2.65rem;
      font-size: 0.95rem;
      color: #0f172a;
      outline: none;
      transition: all 0.2s ease;
      margin: 0;
      min-height: 48px;
    }
    .search-input:focus {
      background: #ffffff;
      border-color: #f97316;
      box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.15);
    }
    .search-clear-btn {
      position: absolute;
      right: 0.5rem;
      background: none;
      border: none;
      color: #94a3b8;
      cursor: pointer;
      padding: 0.5rem;
      display: none;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      width: 36px;
      height: 36px;
      min-width: 36px;
      min-height: 36px;
      transition: background 0.15s, color 0.15s;
    }
    .search-clear-btn:hover {
      color: #0f172a;
      background: #f1f5f9;
    }
    
    /* Category Filter Chips */
    .filter-pills-wrap {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      overflow-x: auto;
      padding-bottom: 0.35rem;
      scrollbar-width: thin;
      -webkit-overflow-scrolling: touch;
    }
    .filter-pills-wrap::-webkit-scrollbar {
      height: 4px;
    }
    .filter-pills-wrap::-webkit-scrollbar-thumb {
      background: #cbd5e1;
      border-radius: 4px;
    }
    .filter-chip {
      display: inline-flex;
      align-items: center;
      gap: 0.35rem;
      padding: 0.45rem 0.85rem;
      border-radius: 9999px;
      font-size: 0.825rem;
      font-weight: 600;
      background: #ffffff;
      color: #475569;
      border: 1px solid #cbd5e1;
      cursor: pointer;
      white-space: nowrap;
      transition: all 0.15s ease;
      user-select: none;
      min-height: 38px;
      text-decoration: none;
    }
    .filter-chip:hover {
      background: #f8fafc;
      border-color: #94a3b8;
      color: #0f172a;
    }
    .filter-chip.active {
      background: #0f172a;
      color: #ffffff;
      border-color: #0f172a;
      box-shadow: 0 2px 6px rgba(15, 23, 42, 0.2);
    }
    .filter-chip .chip-count {
      font-size: 0.72rem;
      background: rgba(0, 0, 0, 0.07);
      padding: 0.1rem 0.45rem;
      border-radius: 9999px;
      font-weight: 700;
    }
    .filter-chip.active .chip-count {
      background: rgba(255, 255, 255, 0.25);
      color: #ffffff;
    }

    /* Secondary Controls: Sort, In-Stock, Count */
    .filter-controls-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      flex-wrap: wrap;
      gap: 0.75rem;
      padding-top: 0.75rem;
      border-top: 1px solid #f1f5f9;
      font-size: 0.85rem;
    }
    .filter-controls-left {
      display: flex;
      align-items: center;
      gap: 0.85rem;
      flex-wrap: wrap;
    }
    .filter-select {
      background: #ffffff;
      border: 1px solid #cbd5e1;
      border-radius: 0.5rem;
      padding: 0.35rem 0.75rem;
      font-size: 0.825rem;
      color: #334155;
      margin: 0;
      min-height: 36px;
      cursor: pointer;
      outline: none;
      font-weight: 500;
    }
    .filter-select:focus {
      border-color: #f97316;
    }
    .checkbox-label {
      display: inline-flex;
      align-items: center;
      gap: 0.4rem;
      font-size: 0.825rem;
      font-weight: 600;
      color: #334155;
      cursor: pointer;
      user-select: none;
      margin: 0;
      min-height: 44px;
      padding: 0 4px;
    }
    .checkbox-label input[type="checkbox"] {
      width: 18px;
      height: 18px;
      margin: 0;
      min-height: 18px;
      accent-color: #f97316;
      cursor: pointer;
    }
    .results-count-text {
      color: var(--text-muted);
      font-size: 0.825rem;
      font-weight: 500;
    }
    .reset-filters-btn {
      background: none;
      border: none;
      color: #c2410c;
      font-weight: 700;
      font-size: 0.825rem;
      cursor: pointer;
      text-decoration: underline;
      padding: 0.35rem 0.65rem;
      min-height: 44px;
      display: inline-flex;
      align-items: center;
    }
    .reset-filters-btn:hover {
      color: #9a3412;
    }

    /* Empty catalog state */
    .empty-catalog-state {
      background: #ffffff;
      border: 1.5px dashed #cbd5e1;
      border-radius: 1rem;
      padding: 3.5rem 1.5rem;
      text-align: center;
      display: none;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 0.75rem;
      margin-top: 1rem;
      grid-column: 1 / -1;
    }
    .empty-catalog-icon {
      font-size: 2.5rem;
      line-height: 1;
    }

    /* Merchant info pill in catalog header */
    .merchant-badge-pill {
      background: #ffffff;
      padding: 0.55rem 0.85rem;
      border-radius: 0.6rem;
      font-size: 0.8rem;
      border: 1px solid #e2e8f0;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
      color: #334155;
      display: inline-flex;
      align-items: center;
      gap: 0.45rem;
      line-height: 1.4;
      max-width: 100%;
    }

    /* Footer styling */
    footer.site-footer {
      border-top: 1px solid var(--card-border);
      background: #ffffff;
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
      opacity: 0.9;
      transition: opacity 0.2s;
    }
    .render-logo-img:hover {
      opacity: 1;
    }
    .footer-bottom {
      border-top: 1px solid #f1f5f9;
      padding-top: 1rem;
      font-size: 0.75rem;
      color: #64748b;
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
      .merchant-badge-pill {
        width: 100%;
      }
      .filter-controls-row {
        flex-direction: column;
        align-items: flex-start;
        gap: 0.65rem;
      }
      .filter-controls-left {
        width: 100%;
        justify-content: space-between;
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
      .checkout-grid {
        grid-template-columns: 1fr !important;
        gap: 1.25rem !important;
      }
      .form-row {
        grid-template-columns: 1fr !important;
        gap: 0.4rem !important;
      }
    }
  </style>
</head>
<body>
  <div class="banner-warning">
    <span>⚠️</span>
    <span><strong>Notice:</strong> Demo Market accepts <strong>Credit/Debit Cards ONLY</strong>. PayPal is <strong>NOT</strong> supported natively &mdash; Use the share button to share item link to <strong>Cartwise AI APK</strong> to buy safely with PayPal Escrow.</span>
  </div>
  <header>
    <div class="header-inner">
      <a href="/" class="brand">
        <span>🛍️ Demo Market</span>
        <span class="brand-badge">Card Only Store</span>
      </a>
      <div class="brand-subtext">
        Mock International Merchant &bull; Partnered with Cartwise AI
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

  <!-- Toast Notification for Share / Deep Link Action -->
  <div id="shareToast">
    <span id="shareToastText">Shared successfully!</span>
  </div>

  <script>
    // Universal Share Function for Demo Market items
    function shareProduct(id, title, price, shipping, img) {
      const origin = window.location.origin;
      const productUrl = origin + '/product/' + id;
      const shareText = 'Buy ' + title + ' on Demo Market for $' + price.toFixed(2) + ' with PayPal Escrow on Cartwise AI: ' + productUrl;
      const deepLink = 'cartwise://product?id=' + encodeURIComponent(id) + '&title=' + encodeURIComponent(title) + '&price=' + price + '&shipping=' + shipping;

      if (navigator.share) {
        navigator.share({
          title: title,
          text: shareText,
          url: productUrl
        }).catch(err => {
          if (err.name !== 'AbortError') {
            triggerDeepLinkOrCopy(deepLink, productUrl);
          }
        });
      } else {
        triggerDeepLinkOrCopy(deepLink, productUrl);
      }
    }

    function shareWithCartwise(id, title, price, shipping, img) {
      const origin = window.location.origin;
      const productUrl = origin + '/product/' + id;
      const deepLink = 'cartwise://product?id=' + encodeURIComponent(id) + '&title=' + encodeURIComponent(title) + '&price=' + price + '&shipping=' + shipping;

      // First attempt native share if on mobile (Cartwise APK handles text/plain)
      if (navigator.share) {
        navigator.share({
          title: 'Buy on Cartwise: ' + title,
          text: productUrl,
          url: productUrl
        }).catch(err => {
          if (err.name !== 'AbortError') {
            window.location.href = deepLink;
          }
        });
      } else {
        window.location.href = deepLink;
        showToast('Opening in Cartwise App... (' + id + ')');
      }
    }

    function triggerDeepLinkOrCopy(deepLink, fallbackUrl) {
      // Attempt deep link
      window.location.href = deepLink;
      if (navigator.clipboard) {
        navigator.clipboard.writeText(fallbackUrl).then(() => {
          showToast('Product link copied! Share with Cartwise App.');
        }).catch(() => {
          showToast('Ready to open in Cartwise App.');
        });
      } else {
        showToast('Ready to open in Cartwise App.');
      }
    }

    function showToast(msg) {
      const toast = document.getElementById('shareToast');
      const text = document.getElementById('shareToastText');
      if (toast && text) {
        text.textContent = msg;
        toast.style.display = 'flex';
        setTimeout(() => {
          toast.style.display = 'none';
        }, 3500);
      }
    }
  </script>
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
    theme: 'white',
    timestamp: new Date().toISOString(),
    productsCount: products.length,
    inStockCount: products.filter(p => p.inStock).length
  });
});

// Catalog Homepage
app.get('/', (req, res) => {
  const { q, category, sort, inStock } = req.query;
  const queryStr = (q || '').trim();
  const activeCat = (category || 'all').toLowerCase();
  const sortMode = sort || 'featured';
  const onlyStock = inStock === 'true';

  const categories = [
    { id: 'all', label: 'All Products' },
    { id: 'audio', label: 'Audio' },
    { id: 'wearables', label: 'Wearables' },
    { id: 'accessories', label: 'Accessories' },
    { id: 'electronics', label: 'Electronics' },
    { id: 'gaming', label: 'Gaming' },
    { id: 'home-office', label: 'Home & Office' },
    { id: 'bags', label: 'Bags' }
  ];

  // Calculate matching items for initial server-side render
  let sortedProducts = [...products];
  if (sortMode === 'price-asc') {
    sortedProducts.sort((a, b) => a.price - b.price);
  } else if (sortMode === 'price-desc') {
    sortedProducts.sort((a, b) => b.price - a.price);
  } else if (sortMode === 'rating-desc') {
    sortedProducts.sort((a, b) => (b.rating || 0) - (a.rating || 0));
  }

  let initialVisibleCount = 0;
  const productCards = sortedProducts.map(p => {
    const matchesCat = (activeCat === 'all' || p.category.toLowerCase() === activeCat);
    const searchBlob = `${p.title} ${p.brand} ${p.category} ${p.description} ${(p.keywordsZh || []).join(' ')} ${(p.features || []).join(' ')}`.toLowerCase();
    const matchesQ = !queryStr || searchBlob.includes(queryStr.toLowerCase());
    const matchesStock = !onlyStock || p.inStock;
    const isVisible = matchesCat && matchesQ && matchesStock;

    if (isVisible) initialVisibleCount++;

    return `
    <div class="card" id="product-${p.id}" 
         data-id="${p.id}" 
         data-category="${p.category.toLowerCase()}" 
         data-price="${p.price}" 
         data-rating="${p.rating || 4.5}" 
         data-instock="${p.inStock}"
         data-search="${escapeHtml(searchBlob)}"
         style="${isVisible ? 'display:flex;' : 'display:none;'}">
      <div class="card-img-wrap">
        <img src="${p.image}" alt="${escapeHtml(p.title)}" class="card-img" loading="lazy" />
        <div style="position: absolute; top: 0.75rem; right: 0.75rem;">
          <span class="badge ${p.inStock ? 'badge-in-stock' : 'badge-out-stock'}">
            ${p.inStock ? 'In Stock' : 'Out of Stock'}
          </span>
        </div>
      </div>
      <div class="card-body">
        <div style="font-size: 0.75rem; color: var(--accent-blue); font-weight: 700; text-transform: uppercase; margin-bottom: 0.25rem;">
          ${escapeHtml(p.brand)} &bull; ${escapeHtml(p.category)}
        </div>
        <h2 class="card-title">${escapeHtml(p.title)}</h2>
        <p class="card-desc">${escapeHtml(p.description)}</p>
        <div class="card-price-row">
          <div>
            <div class="price">$${p.price.toFixed(2)}</div>
            <div class="shipping-tag">+$${p.shippingCost.toFixed(2)} shipping</div>
          </div>
          <div class="card-actions">
            <!-- Share Button -->
            <button 
              type="button" 
              class="btn btn-share" 
              onclick="shareProduct('${p.id}', '${p.title.replace(/'/g, "\\'")}', ${p.price}, ${p.shippingCost}, '${p.image}')" 
              title="Share item link to Cartwise AI APK"
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="18" cy="5" r="3"></circle>
                <circle cx="6" cy="12" r="3"></circle>
                <circle cx="18" cy="19" r="3"></circle>
                <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"></line>
                <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"></line>
              </svg>
              <span>Share</span>
            </button>

            <!-- Dark View Item Button -->
            <a href="/product/${p.id}" class="btn btn-dark" style="padding: 0.45rem 0.85rem; font-size: 0.825rem; min-height: 38px;">
              View Item
            </a>
          </div>
        </div>
      </div>
    </div>
  `;
  }).join('');

  const isFiltered = queryStr !== '' || activeCat !== 'all' || onlyStock || sortMode !== 'featured';

  const categoryChips = categories.map(c => {
    const count = c.id === 'all' ? products.length : products.filter(p => p.category.toLowerCase() === c.id).length;
    const isActive = c.id === activeCat;
    return `
      <button type="button" class="filter-chip ${isActive ? 'active' : ''}" data-category="${c.id}">
        <span>${c.label}</span>
        <span class="chip-count">${count}</span>
      </button>
    `;
  }).join('');

  const content = `
    <!-- Catalog Header -->
    <div class="catalog-header" style="margin-bottom: 1.5rem; display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 1rem;">
      <div>
        <h1 style="font-size: 1.85rem; font-weight: 800; color: #0f172a; margin-bottom: 0.35rem; letter-spacing: -0.02em;">Explore All Products</h1>
        <p style="color: var(--text-muted); font-size: 0.925rem;">
          Curated electronics, audio, and gadgets. All products ship globally including Lagos, Nigeria.
        </p>
      </div>
      <div class="merchant-badge-pill">
        <span>💳</span>
        <span><strong>Card Only Merchant</strong> &bull; Share item link to Cartwise AI APK for PayPal Escrow</span>
      </div>
    </div>

    <!-- Search & Filter Bar Section -->
    <section class="search-filter-section" aria-label="Search and filter products">
      <!-- Search Input Box -->
      <div class="search-input-wrap">
        <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8"></circle>
          <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
        </svg>
        <input 
          type="text" 
          id="catalogSearchInput" 
          class="search-input" 
          placeholder="Search products by name, brand, or feature (e.g. earbuds, 65W, ANC, keyboard)..." 
          value="${escapeHtml(queryStr)}"
          autocomplete="off"
          aria-label="Search products by title, brand, or specs"
        />
        <button type="button" id="catalogSearchClear" class="search-clear-btn" aria-label="Clear search" title="Clear search" style="${queryStr ? 'display:flex;' : 'display:none;'}">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>
      </div>

      <!-- Category Filter Chips -->
      <div class="filter-pills-wrap" id="catalogCategoryChips" role="tablist" aria-label="Product categories">
        ${categoryChips}
      </div>

      <!-- Secondary Controls: Sort, In Stock, Count -->
      <div class="filter-controls-row">
        <div class="filter-controls-left">
          <label class="checkbox-label" for="catalogInStockCheck">
            <input type="checkbox" id="catalogInStockCheck" ${onlyStock ? 'checked' : ''} />
            <span>In Stock Only</span>
          </label>

          <div style="display: flex; align-items: center; gap: 0.4rem;">
            <span style="font-size: 0.8rem; color: var(--text-muted); font-weight: 500;">Sort:</span>
            <select id="catalogSortSelect" class="filter-select" aria-label="Sort products">
              <option value="featured" ${sortMode === 'featured' ? 'selected' : ''}>Featured</option>
              <option value="price-asc" ${sortMode === 'price-asc' ? 'selected' : ''}>Price: Low to High</option>
              <option value="price-desc" ${sortMode === 'price-desc' ? 'selected' : ''}>Price: High to Low</option>
              <option value="rating-desc" ${sortMode === 'rating-desc' ? 'selected' : ''}>Highest Rated</option>
            </select>
          </div>
        </div>

        <div style="display: flex; align-items: center; gap: 0.75rem;">
          <span id="catalogResultCount" class="results-count-text">Showing ${initialVisibleCount} of ${products.length} products</span>
          <button type="button" id="catalogResetBtn" class="reset-filters-btn" onclick="resetAllCatalogFilters()" style="${isFiltered ? 'display:inline-block;' : 'display:none;'}">
            Reset Filters
          </button>
        </div>
      </div>
    </section>

    <!-- Product Grid -->
    <div class="grid" id="catalogGrid">
      ${productCards}

      <!-- Empty State -->
      <div id="catalogEmptyState" class="empty-catalog-state" style="${initialVisibleCount === 0 ? 'display:flex;' : 'display:none;'}">
        <div class="empty-catalog-icon">🔍</div>
        <h3 style="font-size: 1.25rem; font-weight: 700; color: #0f172a;">No products found</h3>
        <p style="color: var(--text-muted); font-size: 0.9rem; max-width: 440px;">
          No items match your filter for <span id="emptyStateQuery" style="font-weight: 600; color: #0f172a;">${queryStr ? `"${escapeHtml(queryStr)}"` : (activeCat !== 'all' ? `Category: ${escapeHtml(activeCat)}` : 'selected filters')}</span>.
        </p>
        <button type="button" class="btn btn-orange" onclick="resetAllCatalogFilters()" style="margin-top: 0.5rem; padding: 0.65rem 1.25rem;">
          Clear All Filters
        </button>
      </div>
    </div>

    <!-- Client-side Search and Filter Script -->
    <script>
      (function() {
        let currentCategory = '${activeCat}';
        let searchQuery = '${queryStr.replace(/'/g, "\\'")}';
        let sortOrder = '${sortMode}';
        let onlyInStock = ${onlyStock ? 'true' : 'false'};

        function initCatalogFilter() {
          const searchInput = document.getElementById('catalogSearchInput');
          const clearBtn = document.getElementById('catalogSearchClear');
          const sortSelect = document.getElementById('catalogSortSelect');
          const inStockCheck = document.getElementById('catalogInStockCheck');
          const categoryChips = document.querySelectorAll('#catalogCategoryChips .filter-chip');

          if (!searchInput) return;

          // Input listener with live filtering
          searchInput.addEventListener('input', function(e) {
            searchQuery = e.target.value.trim();
            if (clearBtn) clearBtn.style.display = searchQuery ? 'flex' : 'none';
            applyFilters();
          });

          // Search clear button
          if (clearBtn) {
            clearBtn.addEventListener('click', function() {
              searchInput.value = '';
              searchQuery = '';
              clearBtn.style.display = 'none';
              searchInput.focus();
              applyFilters();
            });
          }

          // Category chip clicks
          categoryChips.forEach(function(chip) {
            chip.addEventListener('click', function() {
              categoryChips.forEach(function(c) { c.classList.remove('active'); });
              chip.classList.add('active');
              currentCategory = chip.getAttribute('data-category') || 'all';
              applyFilters();
            });
          });

          // Sort select
          if (sortSelect) {
            sortSelect.addEventListener('change', function(e) {
              sortOrder = e.target.value;
              applyFilters();
            });
          }

          // In-stock toggle
          if (inStockCheck) {
            inStockCheck.addEventListener('change', function(e) {
              onlyInStock = e.target.checked;
              applyFilters();
            });
          }
        }

        window.applyFilters = function() {
          const cards = Array.from(document.querySelectorAll('#catalogGrid .card'));
          const emptyState = document.getElementById('catalogEmptyState');
          const countDisplay = document.getElementById('catalogResultCount');
          const resetBtn = document.getElementById('catalogResetBtn');
          const emptyQuery = document.getElementById('emptyStateQuery');
          const queryLower = searchQuery.toLowerCase();

          let visibleCount = 0;

          cards.forEach(function(card) {
            const cardCat = (card.getAttribute('data-category') || '').toLowerCase();
            const cardSearch = card.getAttribute('data-search') || '';
            const inStock = card.getAttribute('data-instock') === 'true';

            let matchesSearch = true;
            if (queryLower) {
              const terms = queryLower.split(/\\s+/).filter(Boolean);
              matchesSearch = terms.every(function(t) { return cardSearch.includes(t); });
            }

            const matchesCat = (currentCategory === 'all' || cardCat === currentCategory);
            const matchesStock = (!onlyInStock || inStock);

            if (matchesSearch && matchesCat && matchesStock) {
              card.style.display = 'flex';
              visibleCount++;
            } else {
              card.style.display = 'none';
            }
          });

          // Dynamic DOM Sorting
          if (sortOrder !== 'featured') {
            const grid = document.getElementById('catalogGrid');
            const sorted = cards.filter(function(c) { return c.style.display !== 'none'; }).sort(function(a, b) {
              const priceA = parseFloat(a.getAttribute('data-price') || 0);
              const priceB = parseFloat(b.getAttribute('data-price') || 0);
              const ratingA = parseFloat(a.getAttribute('data-rating') || 0);
              const ratingB = parseFloat(b.getAttribute('data-rating') || 0);

              if (sortOrder === 'price-asc') return priceA - priceB;
              if (sortOrder === 'price-desc') return priceB - priceA;
              if (sortOrder === 'rating-desc') return ratingB - ratingA;
              return 0;
            });

            sorted.forEach(function(card) { grid.appendChild(card); });
          }

          // Counter and Reset button
          if (countDisplay) {
            countDisplay.textContent = 'Showing ' + visibleCount + ' of ' + cards.length + ' products';
          }

          const isFiltered = (searchQuery !== '' || currentCategory !== 'all' || onlyInStock || sortOrder !== 'featured');
          if (resetBtn) {
            resetBtn.style.display = isFiltered ? 'inline-block' : 'none';
          }

          if (emptyState) {
            if (visibleCount === 0) {
              emptyState.style.display = 'flex';
              if (emptyQuery) {
                emptyQuery.textContent = searchQuery ? ('"' + searchQuery + '"') : (currentCategory !== 'all' ? ('Category: ' + currentCategory) : 'selected filters');
              }
            } else {
              emptyState.style.display = 'none';
            }
          }

          // Sync URL params without page reload
          const params = new URLSearchParams();
          if (searchQuery) params.set('q', searchQuery);
          if (currentCategory !== 'all') params.set('category', currentCategory);
          if (sortOrder !== 'featured') params.set('sort', sortOrder);
          if (onlyInStock) params.set('inStock', 'true');
          const newUrl = window.location.pathname + (params.toString() ? '?' + params.toString() : '');
          window.history.replaceState({}, '', newUrl);
        };

        window.resetAllCatalogFilters = function() {
          const searchInput = document.getElementById('catalogSearchInput');
          const clearBtn = document.getElementById('catalogSearchClear');
          const sortSelect = document.getElementById('catalogSortSelect');
          const inStockCheck = document.getElementById('catalogInStockCheck');
          const categoryChips = document.querySelectorAll('#catalogCategoryChips .filter-chip');

          if (searchInput) searchInput.value = '';
          if (clearBtn) clearBtn.style.display = 'none';
          if (sortSelect) sortSelect.value = 'featured';
          if (inStockCheck) inStockCheck.checked = false;

          categoryChips.forEach(function(chip) {
            if ((chip.getAttribute('data-category') || '') === 'all') {
              chip.classList.add('active');
            } else {
              chip.classList.remove('active');
            }
          });

          searchQuery = '';
          currentCategory = 'all';
          sortOrder = 'featured';
          onlyInStock = false;

          applyFilters();
        };

        if (document.readyState === 'loading') {
          document.addEventListener('DOMContentLoaded', initCatalogFilter);
        } else {
          initCatalogFilter();
        }
      })();
    </script>
  `;

  res.send(renderPage('Home - Shop Gadgets & Tech', content));
});

// Product Detail Page
app.get('/product/:id', (req, res) => {
  const product = products.find(p => p.id === req.params.id);
  if (!product) {
    return res.status(404).send(renderPage('Not Found', '<h1>Product Not Found</h1><a href="/" class="btn btn-dark" style="margin-top:1rem;">Back to Store</a>'));
  }

  const variantOptions = product.variants.map((v, i) => `
    <option value="${v.sku}" ${i === 0 ? 'selected' : ''}>${v.name} (${v.sku})</option>
  `).join('');

  const featuresList = product.features.map(f => `
    <li style="margin-bottom: 0.5rem; display: flex; align-items: center; gap: 0.5rem;">
      <span style="color: var(--success); font-weight: bold;">✓</span> <span>${f}</span>
    </li>
  `).join('');

  const content = `
    <div style="margin-bottom: 1.5rem;">
      <a href="/" style="color: #475569; text-decoration: none; font-size: 0.85rem; display: inline-flex; align-items: center; gap: 0.35rem; font-weight: 600; min-height: 44px; padding: 0.25rem 0.5rem;">
        <span>←</span> <span>Back to All Products</span>
      </a>
    </div>
    <div class="product-detail-grid">
      <!-- Real Product Image Box -->
      <div class="product-detail-image-box">
        <div style="width: 100%; max-width: 320px; height: 280px; display: flex; align-items: center; justify-content: center;">
          <img src="${product.image}" alt="${product.title}" class="product-detail-img" />
        </div>
        <div style="margin-top: 1.25rem; display: flex; gap: 0.5rem; flex-wrap: wrap; justify-content: center;">
          <span class="badge ${product.inStock ? 'badge-in-stock' : 'badge-out-stock'}">
            ${product.inStock ? `In Stock (${product.stockCount} left)` : 'Out of Stock'}
          </span>
          <span class="badge" style="background: #f1f5f9; color: #475569; border: 1px solid #cbd5e1;">SKU: ${product.variants[0].sku}</span>
        </div>
      </div>

      <!-- Product Info & Purchase Form -->
      <div>
        <div style="display: flex; align-items: center; justify-content: space-between; gap: 0.5rem;">
          <div style="font-size: 0.8rem; color: var(--accent-blue); font-weight: 700; text-transform: uppercase;">
            ${product.brand} &bull; ${product.category}
          </div>
          <!-- Top Share Button -->
          <button 
            type="button" 
            class="btn btn-share" 
            onclick="shareProduct('${product.id}', '${product.title.replace(/'/g, "\\'")}', ${product.price}, ${product.shippingCost}, '${product.image}')"
            title="Share this product"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="18" cy="5" r="3"></circle>
              <circle cx="6" cy="12" r="3"></circle>
              <circle cx="18" cy="19" r="3"></circle>
              <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"></line>
              <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"></line>
            </svg>
            <span>Share</span>
          </button>
        </div>

        <h1 style="font-size: 1.75rem; font-weight: 800; color: #0f172a; margin: 0.5rem 0 0.75rem; line-height: 1.3; letter-spacing: -0.02em;">
          ${product.title}
        </h1>
        <div style="display: flex; align-items: baseline; gap: 0.75rem; margin-bottom: 1.25rem; flex-wrap: wrap;">
          <div style="font-size: 2rem; font-weight: 800; color: #0f172a;">$${product.price.toFixed(2)}</div>
          <div style="font-size: 0.9rem; color: var(--accent-blue); font-weight: 600;">
            + $${product.shippingCost.toFixed(2)} shipping to Lagos, NG
          </div>
        </div>

        <p style="color: #475569; line-height: 1.6; margin-bottom: 1.25rem; font-size: 0.925rem;">
          ${product.description}
        </p>

        <ul style="list-style: none; margin-bottom: 1.5rem; font-size: 0.875rem; color: #475569;">
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
            <!-- Orange Gradient Checkout Button -->
            <button type="submit" class="btn btn-orange" id="buyNowBtn" style="width: 100%; padding: 0.9rem; font-size: 1.05rem; margin-top: 0.5rem;">
              Proceed to Card Checkout ($${(product.price + product.shippingCost).toFixed(2)})
            </button>

            <!-- Dark Cartwise APK Share / Buy Button -->
            <button 
              type="button" 
              class="btn btn-dark" 
              onclick="shareWithCartwise('${product.id}', '${product.title.replace(/'/g, "\\'")}', ${product.price}, ${product.shippingCost}, '${product.image}')" 
              style="width: 100%; padding: 0.85rem; font-size: 0.95rem; margin-top: 0.65rem;"
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#38bdf8" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path>
                <line x1="3" y1="6" x2="21" y2="6"></line>
                <path d="M16 10a4 4 0 0 1-8 0"></path>
              </svg>
              <span>Share to Cartwise APK (Pay with PayPal Escrow)</span>
            </button>

            <!-- Safe Escrow Protection Tip -->
            <div style="margin-top: 0.85rem; padding: 0.65rem 0.85rem; background: #f0f9ff; border: 1px solid #bae6fd; border-radius: 0.6rem; text-align: center; font-size: 0.8rem; color: #0369a1; line-height: 1.45;">
              🔒 <strong>Safe Escrow Tip:</strong> Use the share button to share item link to <strong>Cartwise AI APK</strong> to buy safely with PayPal Escrow.
            </div>
          ` : `
            <div style="background: #fef2f2; border: 1px solid #fecaca; color: #b91c1c; padding: 1rem; border-radius: 0.5rem; text-align: center; font-weight: 600; margin-top: 0.5rem;">
              ⚠️ This item is currently Out of Stock.
            </div>
          `}
        </form>

        <!-- Admin toggle for hackathon live demo failure simulation -->
        <div style="margin-top: 1.5rem; padding: 0.85rem 1rem; background: #f8fafc; border-radius: 0.6rem; border: 1px dashed #cbd5e1; font-size: 0.75rem;">
          <div style="color: #b45309; font-weight: 700; margin-bottom: 0.35rem;">Hackathon Test Control:</div>
          <button onclick="toggleStock('${product.id}')" class="btn btn-share" style="padding: 0.4rem 0.8rem; font-size: 0.75rem;">
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
      <h1 style="font-size: 1.85rem; font-weight: 800; color: #0f172a; margin-bottom: 0.35rem; letter-spacing: -0.02em;">Secure Checkout</h1>
      <p style="color: var(--text-muted); margin-bottom: 1.75rem; font-size: 0.9rem;">Demo Market Store &bull; Standard Credit/Debit Card Order</p>

      ${!product.inStock ? `
        <div style="background: #fef2f2; border: 1px solid #fecaca; color: #b91c1c; padding: 1.25rem; border-radius: 0.75rem; margin-bottom: 1.5rem; text-align: center;">
          <h3 style="margin-bottom: 0.35rem;">⚠️ Cannot Proceed: Item Out of Stock</h3>
          <p>The product <strong>${product.title}</strong> has sold out.</p>
        </div>
      ` : ''}

      <div class="checkout-grid">
        <!-- Checkout Form -->
        <div style="background: var(--card-bg); border: 1px solid var(--card-border); border-radius: 1rem; padding: 1.75rem; box-shadow: 0 1px 3px rgba(0,0,0,0.04);">
          <form id="checkoutForm" action="/api/checkout" method="POST">
            <input type="hidden" name="productId" value="${product.id}">
            <input type="hidden" name="quantity" value="${quantity}">
            <input type="hidden" name="variant" value="${variant}">

            <h3 style="font-size: 1.1rem; font-weight: 700; color: #0f172a; margin-bottom: 1rem; padding-bottom: 0.5rem; border-bottom: 1px solid var(--card-border);">
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

            <h3 style="font-size: 1.1rem; font-weight: 700; color: #0f172a; margin: 1.5rem 0 1rem; padding-bottom: 0.5rem; border-bottom: 1px solid var(--card-border);">
              2. Payment Information
            </h3>

            <div style="background: #eff6ff; border: 1px solid #bfdbfe; color: #1e40af; padding: 0.75rem 1rem; border-radius: 0.5rem; font-size: 0.825rem; margin-bottom: 1rem;">
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

            <div id="checkoutError" style="display: none; background: #fef2f2; border: 1px solid #fecaca; color: #b91c1c; padding: 0.75rem; border-radius: 0.5rem; font-size: 0.85rem; margin-bottom: 1rem;"></div>

            <!-- Orange Gradient Submit Button -->
            <button type="submit" id="placeOrderBtn" class="btn btn-orange" style="width: 100%; padding: 1rem; font-size: 1.05rem;" ${!product.inStock ? 'disabled style="opacity:0.5; cursor:not-allowed;"' : ''}>
              Place Order ($${total.toFixed(2)})
            </button>
          </form>
        </div>

        <!-- Order Summary Sidebar -->
        <div>
          <div style="background: var(--card-bg); border: 1px solid var(--card-border); border-radius: 1rem; padding: 1.5rem; position: sticky; top: 90px; box-shadow: 0 1px 3px rgba(0,0,0,0.04);">
            <h3 style="font-size: 1.1rem; font-weight: 700; color: #0f172a; margin-bottom: 1.25rem;">
              Order Summary
            </h3>

            <div style="display: flex; gap: 1rem; padding-bottom: 1.25rem; border-bottom: 1px solid var(--card-border); margin-bottom: 1.25rem;">
              <div style="width: 70px; height: 70px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 0.5rem; padding: 0.35rem; display: flex; align-items: center; justify-content: center; overflow: hidden;">
                <img src="${product.image}" alt="${product.title}" style="max-width: 100%; max-height: 100%; object-fit: contain;" />
              </div>
              <div style="flex: 1; min-width: 0;">
                <div style="font-size: 0.9rem; font-weight: 700; color: #0f172a; line-height: 1.3;">${product.title}</div>
                <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.25rem;">Variant: ${variant}</div>
                <div style="font-size: 0.8rem; color: var(--text-muted);">Qty: ${quantity} &times; $${product.price.toFixed(2)}</div>
              </div>
            </div>

            <div style="font-size: 0.9rem; color: #475569; display: flex; justify-content: space-between; margin-bottom: 0.75rem;">
              <span>Subtotal</span>
              <span id="summarySubtotal" style="font-weight: 600; color: #0f172a;">$${itemTotal.toFixed(2)}</span>
            </div>
            <div style="font-size: 0.9rem; color: #475569; display: flex; justify-content: space-between; margin-bottom: 0.75rem;">
              <span>Shipping to Lagos</span>
              <span id="summaryShipping" style="font-weight: 600; color: #0f172a;">$${shipping.toFixed(2)}</span>
            </div>
            <div style="font-size: 0.9rem; color: #475569; display: flex; justify-content: space-between; margin-bottom: 1.25rem;">
              <span>Estimated Tax</span>
              <span style="font-weight: 600; color: #0f172a;">$0.00</span>
            </div>

            <div style="display: flex; justify-content: space-between; align-items: baseline; padding-top: 1.25rem; border-top: 1px solid var(--card-border); font-size: 1.25rem; font-weight: 800; color: #0f172a;">
              <span>Total</span>
              <span id="summaryTotal" style="color: #ea580c;">$${total.toFixed(2)}</span>
            </div>

            <div style="margin-top: 1.5rem; background: #f8fafc; border: 1px solid #e2e8f0; padding: 0.75rem; border-radius: 0.5rem; font-size: 0.75rem; color: #64748b; text-align: center;">
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
    return res.send(renderPage('Order Not Found', '<h1>Order Not Found</h1><a href="/" class="btn btn-dark" style="margin-top:1rem;">Back to Store</a>'));
  }

  const content = `
    <div style="max-width: 650px; margin: 2rem auto; background: var(--card-bg); border: 1px solid var(--card-border); border-radius: 1.25rem; padding: 2.5rem; text-align: center; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.04);">
      <div style="width: 72px; height: 72px; background: #ecfdf5; color: #059669; border: 2px solid #a7f3d0; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 2.25rem; margin: 0 auto 1.5rem;">
        ✓
      </div>
      <h1 style="font-size: 1.85rem; font-weight: 800; color: #0f172a; margin-bottom: 0.5rem; letter-spacing: -0.02em;">Order Confirmed!</h1>
      <p style="color: var(--text-muted); font-size: 0.95rem; margin-bottom: 2rem;">
        Thank you for your purchase. We have received your card payment and your order is being prepared.
      </p>

      <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 0.75rem; padding: 1.5rem; text-align: left; margin-bottom: 2rem;">
        <div style="display: flex; justify-content: space-between; border-bottom: 1px solid #e2e8f0; padding-bottom: 0.75rem; margin-bottom: 0.75rem;">
          <span style="color: var(--text-muted);">Merchant Order ID:</span>
          <span id="confirmationOrderId" style="color: #0f172a; font-weight: 700; font-family: monospace;">${order.orderId}</span>
        </div>
        <div style="display: flex; justify-content: space-between; border-bottom: 1px solid #e2e8f0; padding-bottom: 0.75rem; margin-bottom: 0.75rem;">
          <span style="color: var(--text-muted);">Item Purchased:</span>
          <span style="color: #0f172a; font-weight: 600;">${order.productTitle}</span>
        </div>
        <div style="display: flex; justify-content: space-between; border-bottom: 1px solid #e2e8f0; padding-bottom: 0.75rem; margin-bottom: 0.75rem;">
          <span style="color: var(--text-muted);">Shipping Address:</span>
          <span style="color: #0f172a; text-align: right;">${order.shipping.address}, ${order.shipping.city}, ${order.shipping.country}</span>
        </div>
        <div style="display: flex; justify-content: space-between; border-bottom: 1px solid #e2e8f0; padding-bottom: 0.75rem; margin-bottom: 0.75rem;">
          <span style="color: var(--text-muted);">Payment Method:</span>
          <span style="color: #0f172a;">Card ending in <strong>${order.cardLast4}</strong></span>
        </div>
        <div style="display: flex; justify-content: space-between; font-size: 1.15rem; font-weight: 800; color: #0f172a; padding-top: 0.5rem;">
          <span>Total Charged:</span>
          <span id="confirmationTotal" style="color: #ea580c;">$${order.total.toFixed(2)}</span>
        </div>
      </div>

      <div style="display: flex; justify-content: center; gap: 1rem;">
        <a href="/" class="btn btn-dark">Shop More Items</a>
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
  const { q, category } = req.query;

  let filtered = products;
  if (category && category.toLowerCase() !== 'all') {
    filtered = filtered.filter(p => p.category.toLowerCase() === category.toLowerCase());
  }
  if (q) {
    const term = q.toLowerCase().trim();
    filtered = filtered.filter(p => {
      const searchBlob = `${p.title} ${p.brand} ${p.category} ${p.description} ${(p.keywordsZh || []).join(' ')} ${(p.features || []).join(' ')}`.toLowerCase();
      return searchBlob.includes(term);
    });
  }

  const formatted = filtered.map(p => ({
    id: p.id,
    title: p.title,
    titleZh: p.titleZh,
    description: p.description,
    descriptionZh: p.descriptionZh,
    keywordsZh: p.keywordsZh || [],
    category: p.category,
    variants: p.variants || [],
    stockCount: p.stockCount,
    image: p.image,
    brand: {
      id: `brand_${p.brand.toLowerCase()}`,
      name: p.brand
    },
    images: [
      `${currentBase}${p.image}`
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

// Get single product details
app.get('/api/products/:id', (req, res) => {
  const product = products.find(p => p.id === req.params.id);
  if (!product) {
    return res.status(404).json({ error: 'Product not found' });
  }
  const host = req.get('host');
  const protocol = req.protocol;
  const currentBase = `${protocol}://${host}`;

  res.json({
    ...product,
    imageUrl: `${currentBase}${product.image}`,
    storeUrl: `${currentBase}/product/${product.id}`
  });
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
