// Cartwise Web Application Client
// Matches Android App Design & Flow 1:1 (Mobile-Phone Optimized)
// PayPal AI Hackathon 2026 Submission

// App State
let currentScreen = 'onboarding';
let searchQuery = '';
let searchResults = [];
let selectedDeal = null;
let selectedVariety = null;
let isSafetyCheckEnabled = false; // Inactive by default per project rules
let selectedGeminiModel = 'gemini-3.8-flash'; // Default to Gemini 3.8 Flash per user request
let isSearching = false;
let orderHistory = [];
let pendingPromptToSearch = null;

// Showcase Carousel Data (Matches Android OnboardingScreen 1:1)
const showcaseItems = [
  {
    title: "Sony WH-1000XM5 ANC Headphones",
    subtitle: "Flagship Noise Cancelling • Multi-Store Index",
    image: "/images/product_headphones.png",
    retailPrice: 1000.00,
    cartwisePrice: 620.00,
    savingsText: "SAVE $380 (38% OFF)"
  },
  {
    title: "Titanium Smartwatch Ultra 49mm",
    subtitle: "Cellular + GPS • International Express",
    image: "/images/product_smartwatch.png",
    retailPrice: 450.00,
    cartwisePrice: 275.00,
    savingsText: "SAVE $175 (39% OFF)"
  },
  {
    title: "Soundcore Life P2 Mini Earbuds",
    subtitle: "Compact True Wireless • Demo Market In-Stock",
    image: "/images/product_earbuds.png",
    retailPrice: 65.00,
    cartwisePrice: 28.99,
    savingsText: "SAVE $36 (55% OFF)"
  }
];

let showcaseIndex = 0;
let showcaseTimer = null;

function initShowcaseCarousel() {
  updateShowcaseUI();
  if (showcaseTimer) clearInterval(showcaseTimer);
  showcaseTimer = setInterval(() => {
    showcaseIndex = (showcaseIndex + 1) % showcaseItems.length;
    updateShowcaseUI();
  }, 4500);
}

function setShowcaseIndex(idx) {
  showcaseIndex = idx;
  updateShowcaseUI();
  if (showcaseTimer) {
    clearInterval(showcaseTimer);
    showcaseTimer = setInterval(() => {
      showcaseIndex = (showcaseIndex + 1) % showcaseItems.length;
      updateShowcaseUI();
    }, 4500);
  }
}

function updateShowcaseUI() {
  const item = showcaseItems[showcaseIndex];
  if (!item) return;

  const img = document.getElementById('showcase-img');
  const title = document.getElementById('showcase-title');
  const subtitle = document.getElementById('showcase-subtitle');
  const retail = document.getElementById('showcase-retail-price');
  const cartwise = document.getElementById('showcase-cartwise-price');
  const savings = document.getElementById('showcase-savings-pill');

  if (img) img.src = item.image;
  if (title) title.textContent = item.title;
  if (subtitle) subtitle.textContent = item.subtitle;
  if (retail) retail.textContent = `$${item.retailPrice.toFixed(0)}`;
  if (cartwise) cartwise.textContent = `$${item.cartwisePrice.toFixed(2)}`;
  if (savings) savings.textContent = item.savingsText;

  // Dots
  const dotsContainer = document.getElementById('showcase-dots');
  if (dotsContainer) {
    dotsContainer.innerHTML = showcaseItems.map((_, idx) => `
      <button 
        onclick="setShowcaseIndex(${idx})" 
        class="${idx === showcaseIndex ? 'w-2.5 h-2.5 bg-cyan-400' : 'w-1.5 h-1.5 bg-gray-600 hover:bg-gray-400'} rounded-full transition-all"
        aria-label="Slide ${idx + 1}"
      ></button>
    `).join('');
  }
}

// Storage Keys
const STORAGE_ORDERS_KEY = 'cartwise_orders_history';
const STORAGE_SHIPPING_KEY = 'cartwise_shipping_address';

// Lifecycle
document.addEventListener('DOMContentLoaded', () => {
  initShowcaseCarousel();
  loadSavedOrderHistory();
  loadSavedShipping();
  updateRiskScoreUI();
  updateModelSelectorUI();
  lucide.createIcons();
});

// Navigation Controller
function navigateTo(screen) {
  currentScreen = screen;
  const screens = ['onboarding', 'ask', 'results', 'confirm', 'agent', 'receipt', 'orders'];
  screens.forEach(s => {
    const el = document.getElementById(`view-${s}`);
    if (el) {
      if (s === screen) {
        el.classList.remove('hidden');
      } else {
        el.classList.add('hidden');
      }
    }
  });

  if (screen === 'orders') {
    renderOrderHistory();
  }

  window.scrollTo({ top: 0, behavior: 'smooth' });
  setTimeout(() => lucide.createIcons(), 50);
}

// Gemini Model Selector
function selectGeminiModel(modelId) {
  selectedGeminiModel = modelId;
  updateModelSelectorUI();
}

function updateModelSelectorUI() {
  const models = ['gemini-3.8-flash', 'gemini-3.7-flash', 'gemini-2.5-flash'];
  models.forEach(m => {
    const btnKey = m.replace('gemini-', '').replace('.', '');
    const btn = document.getElementById(`model-btn-${btnKey}`);
    if (btn) {
      if (m === selectedGeminiModel) {
        btn.className = 'px-2.5 py-2 rounded-xl text-xs font-bold border transition bg-indigo-600/30 border-cyan-400 text-cyan-300 shadow-sm whitespace-nowrap text-center cursor-pointer';
      } else {
        btn.className = 'px-2.5 py-2 rounded-xl text-xs font-medium border transition bg-[#070b14] border-gray-800 text-gray-400 hover:text-gray-200 whitespace-nowrap text-center cursor-pointer';
      }
    }
  });

  const badge = document.getElementById('active-model-badge');
  if (badge) {
    if (selectedGeminiModel === 'gemini-3.8-flash') badge.textContent = 'Gemini 3.8 Flash (Active)';
    else if (selectedGeminiModel === 'gemini-3.7-flash') badge.textContent = 'Gemini 3.7 Flash (Active)';
    else badge.textContent = 'Gemini 2.5 Flash (Active)';
  }
}

// Android APK Modal Controller
function showApkModal() {
  const modal = document.getElementById('apk-modal');
  if (modal) {
    modal.classList.remove('hidden');
    lucide.createIcons();
  }
}

function hideApkModal() {
  const modal = document.getElementById('apk-modal');
  if (modal) modal.classList.add('hidden');
}

function scrollToApkSection() {
  const sec = document.getElementById('section-android-app');
  if (sec) {
    if (currentScreen !== 'onboarding') {
      navigateTo('onboarding');
      setTimeout(() => sec.scrollIntoView({ behavior: 'smooth' }), 120);
    } else {
      sec.scrollIntoView({ behavior: 'smooth' });
    }
  }
}

// AI Merchant Risk Score Toggle Handling
function toggleRiskScoreClick() {
  isSafetyCheckEnabled = !isSafetyCheckEnabled;
  const toggle = document.getElementById('risk-score-toggle');
  if (toggle) toggle.checked = isSafetyCheckEnabled;
  updateRiskScoreUI();
}

function syncRiskScoreToggle(checked) {
  isSafetyCheckEnabled = checked;
  const toggle = document.getElementById('risk-score-toggle');
  if (toggle) toggle.checked = checked;
  updateRiskScoreUI();
  if (currentScreen === 'results') {
    renderDeals();
  }
}

function updateRiskScoreUI() {
  const card = document.getElementById('risk-score-card');
  const iconWrap = document.getElementById('risk-score-icon-wrap');
  const icon = document.getElementById('risk-score-icon');
  const text = document.getElementById('risk-score-status-text');
  const toggle = document.getElementById('risk-score-toggle');
  const resToggle = document.getElementById('results-risk-toggle');

  if (toggle) toggle.checked = isSafetyCheckEnabled;
  if (resToggle) resToggle.checked = isSafetyCheckEnabled;

  if (isSafetyCheckEnabled) {
    if (card) card.className = 'flex items-center justify-between p-3 rounded-xl bg-[#042f2e]/40 border border-emerald-500/50 cursor-pointer transition select-none';
    if (iconWrap) iconWrap.className = 'w-8 h-8 rounded-full bg-emerald-950 flex items-center justify-center transition';
    if (icon) {
      icon.className = 'w-4 h-4 text-emerald-400';
      icon.setAttribute('data-lucide', 'shield-check');
    }
    if (text) {
      text.textContent = 'Risk score active';
      text.className = 'text-[11px] text-emerald-400 font-bold mt-0.5';
    }
  } else {
    if (card) card.className = 'flex items-center justify-between p-3 rounded-xl bg-[#070b14] border border-[#1e293b] cursor-pointer transition select-none';
    if (iconWrap) iconWrap.className = 'w-8 h-8 rounded-full bg-gray-800 flex items-center justify-center transition';
    if (icon) {
      icon.className = 'w-4 h-4 text-gray-400';
      icon.setAttribute('data-lucide', 'shield');
    }
    if (text) {
      text.textContent = 'Risk score inactive';
      text.className = 'text-[11px] text-gray-400 font-normal mt-0.5';
    }
  }
  lucide.createIcons();
}

// Suggested Search Trigger
function selectSuggestedPrompt(prompt) {
  const input = document.getElementById('search-input');
  if (input) input.value = prompt;
  executeSearch(prompt);
}

// Search Execution
function handleSearchSubmit(e) {
  if (e) e.preventDefault();
  const input = document.getElementById('search-input');
  if (input && input.value.trim()) {
    executeSearch(input.value.trim());
  }
}

async function executeSearch(query) {
  if (!query || isSearching) return;
  searchQuery = query;
  isSearching = true;

  const btn = document.getElementById('btn-search-main');
  const label = document.getElementById('btn-search-label');
  btn.disabled = true;
  label.textContent = selectedGeminiModel.includes('3.8') ? 'Gemini 3.8 is searching deals...' : 'Gemini is searching deals...';

  try {
    const isZh = /[\u4e00-\u9fa5]/.test(query);
    const res = await fetch('/api/search', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept-Language': isZh ? 'zh-CN,zh;q=0.9,en-US;q=0.8' : 'en-US,en;q=0.9'
      },
      body: JSON.stringify({
        query,
        country: 'NG',
        safetyCheck: isSafetyCheckEnabled,
        language: isZh ? 'zh' : 'en',
        model: selectedGeminiModel
      })
    });

    if (res.ok) {
      const data = await res.json();
      searchResults = data.deals || [];
    } else {
      console.warn('Backend search error, falling back to local catalog');
      searchResults = getFallbackDeals(query);
    }
  } catch (err) {
    console.warn('Network error during search, falling back to local catalog:', err);
    searchResults = getFallbackDeals(query);
  } finally {
    isSearching = false;
    btn.disabled = false;
    label.textContent = 'Find Best Landed Deal';

    // Set default selected variety
    searchResults.forEach(d => {
      if (d.varieties && d.varieties.length > 0 && !d.selectedVarietyName) {
        d.selectedVarietyName = d.varieties[0].name;
      }
    });

    const queryLabel = document.getElementById('results-query-label');
    if (queryLabel) queryLabel.textContent = `"${query}"`;

    renderDeals();
    navigateTo('results');
  }
}

// Render Results Screen
function renderDeals() {
  const container = document.getElementById('deals-container');
  if (!container) return;

  if (searchResults.length === 0) {
    container.innerHTML = `
      <div class="py-12 text-center text-gray-400 space-y-2 bg-[#0f172a] rounded-2xl border border-[#1e293b] p-6">
        <i data-lucide="package-search" class="w-10 h-10 mx-auto text-gray-600"></i>
        <div class="text-sm font-bold text-gray-200">No products found matching "${searchQuery}"</div>
        <p class="text-xs">Try searching for "earbuds under $40", "fitness tracker", or "keyboard".</p>
      </div>
    `;
    lucide.createIcons();
    return;
  }

  container.innerHTML = searchResults.map(deal => {
    const currentVariety = (deal.varieties || []).find(v => v.name === deal.selectedVarietyName) || deal.varieties?.[0];
    const isSoldOut = currentVariety ? !currentVariety.inStock || currentVariety.stockCount <= 0 : !deal.inStock;

    // AI Safety badge HTML (visible only if toggled active)
    let safetyHtml = '';
    if (isSafetyCheckEnabled && deal.safety) {
      const s = deal.safety;
      let badgeColor = 'bg-emerald-950 text-emerald-400 border-emerald-800';
      let levelLabel = 'GOOD TO BUY';
      if (s.level === 'CAUTION') {
        badgeColor = 'bg-amber-950 text-amber-400 border-amber-800';
        levelLabel = 'CAUTION: THIRD-PARTY';
      } else if (s.level === 'RISKY') {
        badgeColor = 'bg-rose-950 text-rose-400 border-rose-800';
        levelLabel = 'RISKY MERCHANT';
      }

      safetyHtml = `
        <div class="p-2.5 rounded-xl bg-[#070b14] border border-[#1e293b] space-y-1 mt-2">
          <div class="flex items-center justify-between text-[10px]">
            <span class="font-bold px-2 py-0.5 rounded ${badgeColor} border flex items-center gap-1">
              <i data-lucide="shield" class="w-3 h-3"></i> ${levelLabel} &bull; ${s.score}/100
            </span>
            <span class="text-gray-400 font-mono">${s.sslVerified ? '🔒 256-bit SSL' : '⚠️ Unverified'}</span>
          </div>
          <p class="text-[10.5px] text-gray-400 leading-tight">${s.summary}</p>
        </div>
      `;
    }

    // Variety pills
    const varietiesHtml = (deal.varieties || []).map(v => {
      const active = v.name === deal.selectedVarietyName;
      let stockText = '';
      if (!v.inStock || v.stockCount <= 0) stockText = '<span class="text-[9px] text-rose-400 font-bold ml-1">(Sold Out)</span>';
      else if (v.stockCount <= 3) stockText = `<span class="text-[9px] text-amber-400 font-bold ml-1">(${v.stockCount} left)</span>`;

      return `
        <button 
          type="button" 
          onclick="selectVariety('${deal.id}', '${v.name.replace(/'/g, "\\'")}')"
          class="px-2 py-1 rounded-lg text-xs font-semibold border transition ${
            active 
              ? 'bg-indigo-600 border-indigo-500 text-white shadow-sm' 
              : 'bg-[#070b14] border-gray-800 text-gray-400 hover:text-gray-200'
          }"
        >
          ${v.name} ${stockText}
        </button>
      `;
    }).join('');

    return `
      <div class="rounded-2xl bg-[#0f172a] border border-[#1e293b] p-4 shadow-xl space-y-3">
        
        <!-- Top Row: Brand & Store Domain Link -->
        <div class="flex items-center justify-between text-xs">
          <span class="font-bold text-gray-400 uppercase tracking-wide text-[10px]">${deal.brand || 'Merchant'}</span>
          <a 
            href="${deal.storeUrl || '#'}" 
            target="_blank" 
            class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-[#070b14] border border-gray-800 text-cyan-400 hover:text-cyan-300 font-medium text-[11px] transition"
          >
            <span>${deal.storeName}</span>
            <i data-lucide="external-link" class="w-3 h-3"></i>
          </a>
        </div>

        <!-- Title & Rating -->
        <div>
          <h3 class="text-sm font-bold text-white line-clamp-2 leading-snug">${deal.title}</h3>
          <div class="flex items-center gap-2 mt-1 text-xs text-gray-400">
            <span class="text-amber-400 font-bold">★ ${deal.rating || '4.8'}</span>
            <span>&bull;</span>
            <span>${deal.reviewsCount || '150'} reviews</span>
            <span>&bull;</span>
            <span class="${isSoldOut ? 'text-rose-400 font-bold' : 'text-emerald-400 font-bold'}">
              ${isSoldOut ? 'Sold Out' : (currentVariety ? `${currentVariety.stockCount} in stock` : 'In Stock')}
            </span>
          </div>
        </div>

        <!-- AI Why This Deal Card -->
        <div class="p-2.5 rounded-xl bg-indigo-950/40 border border-indigo-900/50 text-xs text-indigo-300 flex items-start gap-2">
          <i data-lucide="sparkles" class="w-3.5 h-3.5 text-cyan-400 shrink-0 mt-0.5"></i>
          <span class="leading-relaxed"><strong>Why this deal:</strong> ${deal.reasonWhy || 'Best overall landed cost with verified delivery to Nigeria.'}</span>
        </div>

        <!-- Variety Selector -->
        <div class="space-y-1.5 pt-0.5">
          <div class="text-[10px] font-bold text-gray-400 uppercase tracking-wider">Choose Variety:</div>
          <div class="flex flex-wrap gap-1.5">
            ${varietiesHtml}
          </div>
          ${isSoldOut ? `
            <div class="text-xs text-rose-400 font-bold bg-rose-950/40 p-2 rounded-lg border border-rose-900/50 mt-1 flex items-center gap-1.5">
              <i data-lucide="alert-circle" class="w-3.5 h-3.5"></i> Option sold out. Please select another variant.
            </div>
          ` : ''}
        </div>

        ${safetyHtml}

        <!-- Landed Cost & Button -->
        <div class="pt-3 border-t border-gray-800/80 space-y-2.5">
          <div class="flex items-baseline justify-between">
            <div>
              <div class="text-[10px] uppercase font-bold text-gray-500">Total Landed Cost</div>
              <div class="text-[11px] text-gray-400">$${deal.price.toFixed(2)} item + $${deal.shippingCost.toFixed(2)} shipping</div>
            </div>
            <div class="text-lg font-extrabold text-emerald-400 font-mono">
              $${deal.totalCost.toFixed(2)}
            </div>
          </div>

          <button 
            type="button"
            onclick="proceedToConfirm('${deal.id}')"
            ${isSoldOut ? 'disabled' : ''}
            class="w-full h-[44px] rounded-xl font-bold text-xs transition flex items-center justify-center gap-1.5 ${
              isSoldOut 
                ? 'bg-gray-800 text-gray-500 cursor-not-allowed border border-gray-700' 
                : 'bg-indigo-600 hover:bg-indigo-500 text-white shadow-md shadow-indigo-600/30'
            }"
          >
            <span>${isSoldOut ? 'Option Sold Out' : 'Select Deal & Review Escrow'}</span>
            <i data-lucide="arrow-right" class="w-3.5 h-3.5"></i>
          </button>
        </div>

      </div>
    `;
  }).join('');

  lucide.createIcons();
}

function selectVariety(dealId, varietyName) {
  const deal = searchResults.find(d => d.id === dealId);
  if (deal) {
    deal.selectedVarietyName = varietyName;
    renderDeals();
  }
}

// Confirm Screen
function proceedToConfirm(dealId) {
  const deal = searchResults.find(d => d.id === dealId);
  if (!deal) return;
  selectedDeal = deal;

  const currentVariety = (deal.varieties || []).find(v => v.name === deal.selectedVarietyName) || deal.varieties?.[0];
  selectedVariety = currentVariety ? currentVariety.name : 'Standard';

  document.getElementById('confirm-brand').textContent = deal.brand || 'Merchant';
  document.getElementById('confirm-title').textContent = deal.title;
  document.getElementById('confirm-variety-pill').textContent = selectedVariety;
  document.getElementById('confirm-item-price').textContent = `$${deal.price.toFixed(2)}`;
  document.getElementById('confirm-shipping-price').textContent = `$${deal.shippingCost.toFixed(2)}`;
  document.getElementById('confirm-total-cost').textContent = `$${deal.totalCost.toFixed(2)}`;
  document.getElementById('btn-authorize-label').textContent = `Authorize Escrow Hold with PayPal ($${deal.totalCost.toFixed(2)})`;

  navigateTo('confirm');
}

function toggleBillingFields(checked) {
  const fields = document.getElementById('billing-fields');
  if (fields) {
    if (checked) fields.classList.add('hidden');
    else fields.classList.remove('hidden');
  }
}

// Trigger Escrow Authorization
function triggerEscrowAuthorization() {
  if (!selectedDeal) return;

  const shipName = document.getElementById('ship-name').value || 'Ada Lovelace';
  const shipAddr = document.getElementById('ship-address').value || '14 Marina Street, Lagos Island';
  const shipCity = document.getElementById('ship-city').value || 'Lagos';
  const shipCountry = document.getElementById('ship-country').value || 'Nigeria';

  const sameAsShipping = document.getElementById('billing-same-check').checked;
  const billName = sameAsShipping ? shipName : (document.getElementById('bill-name').value || shipName);
  const billAddr = sameAsShipping ? shipAddr : (document.getElementById('bill-address').value || shipAddr);

  saveShippingAddress({ fullName: shipName, address: shipAddr, city: shipCity, country: shipCountry });

  navigateTo('agent');

  executeAgentSimulation({
    deal: selectedDeal,
    variety: selectedVariety,
    shippingAddress: { fullName: shipName, address: shipAddr, city: shipCity, country: shipCountry },
    billingAddress: { cardholderName: billName, address: billAddr }
  });
}

// Agent Simulation Runner
function executeAgentSimulation({ deal, variety, shippingAddress }) {
  const isDemo = deal.storeUrl.includes('cartwise-demo-market') || deal.storeName.includes('Demo Market');
  const logsEl = document.getElementById('agent-logs');
  logsEl.innerHTML = `<div>[00:01] Orders v2 authorization payload accepted (intent=AUTHORIZE).</div>`;

  const addLog = (msg) => {
    const time = new Date().toLocaleTimeString();
    logsEl.innerHTML += `<div>[${time}] ${msg}</div>`;
    logsEl.scrollTop = logsEl.scrollHeight;
  };

  const setStepState = (stepNum, state) => {
    const stepEl = document.getElementById(`step-${stepNum}`);
    const iconEl = document.getElementById(`step-${stepNum}-icon`);
    if (!stepEl) return;

    if (state === 'active') {
      stepEl.className = 'p-3 rounded-xl bg-indigo-950/60 border border-indigo-500 transition animate-pulse flex items-center gap-3';
      if (iconEl) iconEl.className = 'w-7 h-7 rounded-full bg-indigo-600 text-white flex items-center justify-center text-xs font-bold shrink-0';
    } else if (state === 'completed') {
      stepEl.className = 'p-3 rounded-xl bg-emerald-950/40 border border-emerald-800/60 transition flex items-center gap-3';
      if (iconEl) {
        iconEl.innerHTML = '✓';
        iconEl.className = 'w-7 h-7 rounded-full bg-emerald-600 text-white flex items-center justify-center text-xs font-bold shrink-0';
      }
    } else if (state === 'failed') {
      stepEl.className = 'p-3 rounded-xl bg-rose-950/60 border border-rose-800 transition flex items-center gap-3';
      if (iconEl) {
        iconEl.innerHTML = '✕';
        iconEl.className = 'w-7 h-7 rounded-full bg-rose-600 text-white flex items-center justify-center text-xs font-bold shrink-0';
      }
    }
  };

  addLog('PayPal Escrow hold locked ($' + deal.totalCost.toFixed(2) + '). Buyer card NOT charged.');

  setTimeout(() => {
    setStepState(2, 'active');
    addLog('Booting isolated cloud browser session via Kernel SDK...');
  }, 1100);

  setTimeout(() => {
    setStepState(2, 'completed');
    setStepState(3, 'active');
    addLog(`Navigating to ${deal.storeName}...`);
    addLog(`Selected variety: "${variety}". Pre-populating shipping to Lagos, Nigeria.`);
  }, 2400);

  setTimeout(() => {
    setStepState(3, 'completed');
    setStepState(4, 'active');
    addLog('Injecting Cartwise purchasing card: 123456*****2234...');
  }, 3800);

  setTimeout(() => {
    if (isDemo) {
      setStepState(4, 'completed');
      setStepState(5, 'active');
      addLog('Merchant confirmed purchase! Order ID: DM-' + Math.floor(100000 + Math.random() * 900000));
      addLog('Executing PayPal Capture POST /v2/payments/authorizations/{id}/capture...');

      setTimeout(() => {
        setStepState(5, 'completed');
        addLog('Capture confirmed! Receipt generated.');

        const order = {
          id: 'cw_' + Date.now(),
          merchantOrderId: 'DM-' + Math.floor(100000 + Math.random() * 900000),
          paypalCaptureId: 'CAP-' + Math.floor(100000 + Math.random() * 900000),
          status: 'CAPTURED',
          deal,
          variety,
          totalAmount: deal.totalCost,
          shippingAddress,
          timestamp: new Date().toLocaleString()
        };

        saveOrderToHistory(order);
        renderReceiptScreen(order);
        navigateTo('receipt');
      }, 1400);

    } else {
      setStepState(4, 'failed');
      addLog('Merchant gateway returned: DECLINED (INSUFFICIENT_FUNDS).');
      addLog('GUARDRAIL TRIGGERED: Automatically voiding PayPal authorization hold...');

      setTimeout(() => {
        const authId = 'AUTH-' + Math.floor(100000 + Math.random() * 900000);
        addLog(`POST /v2/payments/authorizations/${authId}/void -> 204 NO CONTENT.`);
        addLog('PayPal escrow released immediately. Buyer charged $0.00.');

        const order = {
          id: 'cw_' + Date.now(),
          paypalAuthId: authId,
          status: 'VOIDED',
          deal,
          variety,
          totalAmount: 0.00,
          originalAmount: deal.totalCost,
          failureReason: 'Merchant declined test card (Insufficient Funds). PayPal escrow automatically released.',
          shippingAddress,
          timestamp: new Date().toLocaleString()
        };

        saveOrderToHistory(order);
        renderReceiptScreen(order);
        navigateTo('receipt');
      }, 1500);
    }
  }, 5200);
}

// Receipt Screen
function renderReceiptScreen(order) {
  const container = document.getElementById('receipt-card');
  if (!container) return;

  const isCaptured = order.status === 'CAPTURED';

  if (isCaptured) {
    container.innerHTML = `
      <div class="text-center space-y-2">
        <div class="w-12 h-12 mx-auto rounded-full bg-emerald-950 border border-emerald-800 flex items-center justify-center text-emerald-400">
          <i data-lucide="check" class="w-6 h-6"></i>
        </div>
        <span class="inline-block px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase bg-emerald-950 text-emerald-400 border border-emerald-800">
          CAPTURED &bull; PAID
        </span>
        <h2 class="text-xl font-bold text-white">Order Confirmed!</h2>
        <p class="text-xs text-gray-400">Escrow captured &bull; Merchant receipt secured</p>
      </div>

      <div class="bg-[#070b14] rounded-xl p-3.5 border border-[#1e293b] space-y-2 text-xs">
        <div class="flex justify-between pb-1.5 border-b border-gray-900">
          <span class="text-gray-400">Merchant Order ID</span>
          <span class="font-mono font-bold text-white">${order.merchantOrderId}</span>
        </div>
        <div class="flex justify-between pb-1.5 border-b border-gray-900">
          <span class="text-gray-400">PayPal Capture ID</span>
          <span class="font-mono text-cyan-400">${order.paypalCaptureId}</span>
        </div>
        <div class="flex justify-between pb-1.5 border-b border-gray-900">
          <span class="text-gray-400">Item</span>
          <span class="font-semibold text-white truncate max-w-[180px]">${order.deal.title} (${order.variety})</span>
        </div>
        <div class="flex justify-between pb-1.5 border-b border-gray-900">
          <span class="text-gray-400">Recipient</span>
          <span class="text-gray-200">${order.shippingAddress.fullName}, ${order.shippingAddress.city}</span>
        </div>
        <div class="flex justify-between pt-1 font-bold text-sm">
          <span class="text-white">Amount Paid</span>
          <span class="font-mono text-emerald-400">$${order.totalAmount.toFixed(2)}</span>
        </div>
      </div>

      <div class="space-y-2 pt-1">
        <button onclick="navigateTo('orders')" class="w-full h-[46px] rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-bold text-xs shadow-md transition flex items-center justify-center gap-1.5">
          <i data-lucide="history" class="w-4 h-4"></i> View in Order History
        </button>
        <button onclick="navigateTo('ask')" class="w-full h-[40px] rounded-xl bg-gray-900 hover:bg-gray-800 text-gray-300 font-semibold text-xs border border-gray-800 transition">
          Shop Again
        </button>
      </div>
    `;
  } else {
    container.innerHTML = `
      <div class="text-center space-y-2">
        <div class="w-12 h-12 mx-auto rounded-full bg-amber-950 border border-amber-800 flex items-center justify-center text-amber-400">
          <i data-lucide="shield-alert" class="w-6 h-6"></i>
        </div>
        <span class="inline-block px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase bg-amber-950 text-amber-400 border border-amber-800">
          VOIDED &bull; $0.00 CHARGED
        </span>
        <h2 class="text-xl font-bold text-white">Escrow Hold Released!</h2>
        <p class="text-xs text-gray-400">Autonomous Guardrail Activated</p>
      </div>

      <div class="p-3 rounded-xl bg-amber-950/40 border border-amber-900/50 text-xs text-amber-300 leading-relaxed">
        <strong>Guardrail Protected:</strong> Merchant declined test card (Insufficient Funds). Cartwise automatically voided your PayPal authorization. <strong>You were charged $0.00.</strong>
      </div>

      <div class="bg-[#070b14] rounded-xl p-3.5 border border-[#1e293b] space-y-2 text-xs">
        <div class="flex justify-between pb-1.5 border-b border-gray-900">
          <span class="text-gray-400">PayPal Auth ID</span>
          <span class="font-mono font-bold text-white">${order.paypalAuthId}</span>
        </div>
        <div class="flex justify-between pb-1.5 border-b border-gray-900">
          <span class="text-gray-400">Status</span>
          <span class="font-bold text-amber-400">RELEASED / VOIDED</span>
        </div>
        <div class="flex justify-between pt-1 font-bold text-sm">
          <span class="text-white">Amount Charged</span>
          <span class="font-mono text-emerald-400">$0.00</span>
        </div>
      </div>

      <div class="space-y-2 pt-1">
        <button onclick="navigateTo('orders')" class="w-full h-[46px] rounded-xl bg-gray-800 hover:bg-gray-700 text-white font-bold text-xs border border-gray-700 transition flex items-center justify-center gap-1.5">
          <i data-lucide="history" class="w-4 h-4"></i> View in Order History
        </button>
        <button onclick="navigateTo('ask')" class="w-full h-[40px] rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-semibold text-xs transition">
          Shop Again
        </button>
      </div>
    `;
  }
  lucide.createIcons();
}

// Order History Persistence
function loadSavedOrderHistory() {
  try {
    const raw = localStorage.getItem(STORAGE_ORDERS_KEY);
    if (raw) {
      const parsed = JSON.parse(raw);
      orderHistory = (Array.isArray(parsed) ? parsed : []).filter(o => {
        const id = String(o.id || '');
        const title = String(o.deal?.title || '');
        return !id.startsWith('ord_demo') && !title.includes('Anker Soundcore P20i');
      });
      updateOrdersCountBadge();
    }
  } catch (e) {
    orderHistory = [];
  }
}

function saveOrderToHistory(order) {
  orderHistory.unshift(order);
  localStorage.setItem(STORAGE_ORDERS_KEY, JSON.stringify(orderHistory));
  updateOrdersCountBadge();
}

function clearOrderHistory() {
  if (confirm('Clear past order history?')) {
    orderHistory = [];
    localStorage.removeItem(STORAGE_ORDERS_KEY);
    updateOrdersCountBadge();
    renderOrderHistory();
  }
}

function updateOrdersCountBadge() {
  const badge = document.getElementById('badge-orders-count');
  if (badge) badge.textContent = orderHistory.length;
}

function renderOrderHistory() {
  const container = document.getElementById('orders-list-container');
  if (!container) return;

  if (orderHistory.length === 0) {
    container.innerHTML = `
      <div class="py-12 text-center text-gray-400 space-y-2 bg-[#0f172a] rounded-2xl border border-[#1e293b] p-6">
        <i data-lucide="clipboard-list" class="w-10 h-10 mx-auto text-gray-600"></i>
        <div class="text-sm font-bold text-gray-200">No Orders Recorded Yet</div>
        <p class="text-xs">Completed purchases and protected escrow releases will appear here.</p>
        <button onclick="navigateTo('ask')" class="px-3.5 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-bold text-xs transition mt-2">
          Start Shopping
        </button>
      </div>
    `;
    lucide.createIcons();
    return;
  }

  container.innerHTML = orderHistory.map(o => {
    const isCaptured = o.status === 'CAPTURED';
    return `
      <div class="rounded-2xl bg-[#0f172a] border border-[#1e293b] p-3.5 shadow-xl space-y-2.5">
        <div class="flex items-center justify-between pb-2 border-b border-gray-800">
          <span class="px-2 py-0.5 rounded text-[9px] font-bold uppercase ${
            isCaptured ? 'bg-emerald-950 text-emerald-400 border border-emerald-800' : 'bg-amber-950 text-amber-400 border border-amber-800'
          }">
            ${isCaptured ? 'CAPTURED • PAID' : 'VOIDED • $0 CHARGED'}
          </span>
          <span class="text-xs font-mono font-bold ${isCaptured ? 'text-emerald-400' : 'text-gray-400'}">
            $${o.totalAmount.toFixed(2)}
          </span>
        </div>

        <div>
          <h4 class="text-xs font-bold text-white line-clamp-1">${o.deal.title}</h4>
          <div class="text-[11px] text-gray-400 mt-0.5 flex items-center gap-1.5">
            <span class="px-1.5 py-0.2 rounded bg-[#070b14] text-gray-300 font-semibold">${o.variety}</span>
            <span>&bull;</span>
            <span>${o.deal.storeName}</span>
          </div>
        </div>

        <div class="pt-1 text-[11px] text-gray-400 flex items-center justify-between border-t border-gray-900">
          <div class="truncate max-w-[180px]">${o.shippingAddress.fullName} (${o.shippingAddress.city})</div>
          <div class="font-mono text-gray-500 text-[10px]">${isCaptured ? o.merchantOrderId : o.paypalAuthId}</div>
        </div>
      </div>
    `;
  }).join('');

  lucide.createIcons();
}

function loadSavedShipping() {
  try {
    const raw = localStorage.getItem(STORAGE_SHIPPING_KEY);
    if (raw) {
      const addr = JSON.parse(raw);
      if (addr.fullName) document.getElementById('ship-name').value = addr.fullName;
      if (addr.address) document.getElementById('ship-address').value = addr.address;
      if (addr.city) document.getElementById('ship-city').value = addr.city;
      if (addr.country) document.getElementById('ship-country').value = addr.country;
    }
  } catch (e) {}
}

function saveShippingAddress(addr) {
  try {
    localStorage.setItem(STORAGE_SHIPPING_KEY, JSON.stringify(addr));
  } catch (e) {}
}

// Fallback Deals (Multilingual Support)
function getFallbackDeals(query) {
  const isZh = /[\u4e00-\u9fa5]/.test(query);

  if (isZh) {
    return [
      {
        id: "prod-earbuds-01",
        title: "SoundPulse Air 无线降噪耳机",
        brand: "SoundPulse",
        price: 34.99,
        shippingCost: 5.00,
        totalCost: 39.99,
        storeName: "Cartwise 官方自营 Demo Market",
        storeUrl: "https://cartwise-demo-market.onrender.com/product/prod-earbuds-01",
        rating: 4.8,
        reviewsCount: 312,
        inStock: true,
        stockCount: 25,
        reasonWhy: "最佳综合性价比：全包到手价仅 $39.99，支持正品保障与快速送达。",
        selectedVarietyName: "曜石黑",
        varieties: [
          { name: "曜石黑", sku: "SP-AIR-BLK", stockCount: 25, inStock: true },
          { name: "珍珠白", sku: "SP-AIR-WHT", stockCount: 12, inStock: true },
          { name: "深海蓝", sku: "SP-AIR-NVY", stockCount: 2, inStock: true },
          { name: "玫瑰金", sku: "SP-AIR-GLD", stockCount: 0, inStock: false }
        ],
        safety: {
          score: 98,
          level: "SAFE",
          sslVerified: true,
          summary: "Cartwise 官方自营测试商户。256位TLS加密，沙箱结算隔离，100%全额托管保障。"
        }
      },
      {
        id: "prod-tracker-02",
        title: "NovaFit Pro 智能运动手环",
        brand: "NovaFit",
        price: 29.99,
        shippingCost: 4.50,
        totalCost: 34.49,
        storeName: "Cartwise 官方自营 Demo Market",
        storeUrl: "https://cartwise-demo-market.onrender.com/product/prod-tracker-02",
        rating: 4.6,
        reviewsCount: 184,
        inStock: true,
        stockCount: 18,
        reasonWhy: "高评分健康监测优选：14天超长续航，AMOLED高清大屏，全包到手价最低。",
        selectedVarietyName: "暗夜黑",
        varieties: [
          { name: "暗夜黑", sku: "NF-PRO-BLK", stockCount: 18, inStock: true },
          { name: "橄榄绿", sku: "NF-PRO-GRN", stockCount: 5, inStock: true },
          { name: "珊瑚粉", sku: "NF-PRO-CRL", stockCount: 0, inStock: false }
        ],
        safety: {
          score: 98,
          level: "SAFE",
          sslVerified: true,
          summary: "Cartwise 官方自营测试商户。256位TLS加密，沙箱结算隔离，100%全额托管保障。"
        }
      }
    ];
  }

  return [
    {
      id: "prod-earbuds-01",
      title: "SoundPulse Air Wireless Noise-Cancelling Earbuds",
      brand: "SoundPulse",
      price: 34.99,
      shippingCost: 5.00,
      totalCost: 39.99,
      storeName: "Cartwise Demo Market",
      storeUrl: "https://cartwise-demo-market.onrender.com/product/prod-earbuds-01",
      rating: 4.8,
      reviewsCount: 312,
      inStock: true,
      stockCount: 25,
      reasonWhy: "Best value pick: lowest total landed cost ($39.99) with verified shipping to Nigeria.",
      selectedVarietyName: "Matte Black",
      varieties: [
        { name: "Matte Black", sku: "SP-AIR-BLK", stockCount: 25, inStock: true },
        { name: "Pearl White", sku: "SP-AIR-WHT", stockCount: 12, inStock: true },
        { name: "Deep Navy", sku: "SP-AIR-NVY", stockCount: 2, inStock: true },
        { name: "Rose Gold", sku: "SP-AIR-GLD", stockCount: 0, inStock: false }
      ],
      safety: {
        score: 98,
        level: "SAFE",
        sslVerified: true,
        summary: "Official Cartwise Sandbox Store. 256-bit TLS encrypted, sandbox payment isolation, 100% money-back escrow guarantee."
      }
    },
    {
      id: "prod-tracker-02",
      title: "NovaFit Pro Smart Fitness Tracker Band",
      brand: "NovaFit",
      price: 29.99,
      shippingCost: 4.50,
      totalCost: 34.49,
      storeName: "Cartwise Demo Market",
      storeUrl: "https://cartwise-demo-market.onrender.com/product/prod-tracker-02",
      rating: 4.6,
      reviewsCount: 184,
      inStock: true,
      stockCount: 18,
      reasonWhy: "Top customer-rated fitness wearable with 14-day battery and AMOLED display under $35.",
      selectedVarietyName: "Midnight Black",
      varieties: [
        { name: "Midnight Black", sku: "NF-PRO-BLK", stockCount: 18, inStock: true },
        { name: "Olive Green", sku: "NF-PRO-GRN", stockCount: 5, inStock: true },
        { name: "Sunset Coral", sku: "NF-PRO-CRL", stockCount: 0, inStock: false }
      ],
      safety: {
        score: 98,
        level: "SAFE",
        sslVerified: true,
        summary: "Official Cartwise Sandbox Store. 256-bit TLS encrypted, sandbox payment isolation, 100% money-back escrow guarantee."
      }
    },
    {
      id: "prod-sony-wh1000",
      title: "Sony WH-1000XM5 Wireless ANC Headphones",
      brand: "Sony",
      price: 38.50,
      shippingCost: 10.00,
      totalCost: 48.50,
      storeName: "Glorious Gaming Boutique",
      storeUrl: "https://www.gloriousgaming.com",
      rating: 4.6,
      reviewsCount: 210,
      inStock: true,
      stockCount: 6,
      reasonWhy: "Popular choice with premium ANC, but third-party merchant with strict card fraud verification.",
      selectedVarietyName: "Midnight Black",
      varieties: [
        { name: "Midnight Black", sku: "var-sony-blk", stockCount: 6, inStock: true },
        { name: "Platinum Silver", sku: "var-sony-slv", stockCount: 1, inStock: true },
        { name: "Space Grey", sku: "var-sony-gry", stockCount: 0, inStock: false }
      ],
      safety: {
        score: 68,
        level: "CAUTION",
        sslVerified: true,
        summary: "Third-party merchant. SSL verified, but strict gateway rules may decline corporate test card. Escrow auto-void safeguard active."
      }
    }
  ];
}
