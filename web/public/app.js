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
        class="w-8 h-8 flex items-center justify-center rounded-full hover:bg-[#E2DFD7]/60 transition cursor-pointer"
        aria-label="Slide ${idx + 1}"
      >
        <span class="${idx === showcaseIndex ? 'w-2.5 h-2.5 bg-[#0D261C] ring-2 ring-[#0D261C]/20' : 'w-2 h-2 bg-[#D5D1C8] hover:bg-[#8C9690]'} rounded-full block transition-all"></span>
      </button>
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
  
  const searchInput = document.getElementById('search-input');
  if (searchInput) {
    searchInput.addEventListener('input', updateSearchButtonState);
    searchInput.addEventListener('keyup', updateSearchButtonState);
    searchInput.addEventListener('change', updateSearchButtonState);
  }
  updateSearchButtonState();

  // Support URL Hash or ?view= for direct deep-linking
  const urlParams = new URLSearchParams(window.location.search);
  const initialView = urlParams.get('view') || window.location.hash.replace(/^#/, '');
  if (initialView) {
    if (initialView === 'prompt-confirm') {
      navigateTo('ask');
      selectSuggestedPrompt('NovaFit Pro Smart Fitness Tracker Band under $35');
    } else if (initialView === 'paypal-sandbox') {
      searchResults = getFallbackDeals('earbuds');
      selectedDeal = searchResults[0];
      selectedVariety = selectedDeal.varieties[0].name;
      proceedToConfirm(selectedDeal.id);
      showPayPalSandboxModal({
        id: 'cw_ord_1794272998876_demo',
        paypalOrderId: '01J588178L663410Y',
        totalAmount: 39.99,
        paypalApproveUrl: 'https://www.sandbox.paypal.com/checkoutnow?token=01J588178L663410Y',
        deal: selectedDeal,
        variety: selectedVariety,
        shippingAddress: { fullName: 'Ada Lovelace', email: 'ada.lovelace@gmail.com' }
      });
    } else if (['onboarding', 'ask', 'results', 'confirm', 'agent', 'receipt', 'orders'].includes(initialView)) {
      if (initialView === 'results' && searchResults.length === 0) {
        searchResults = getFallbackDeals('earbuds');
        renderDeals();
      } else if (initialView === 'confirm' && !selectedDeal) {
        searchResults = getFallbackDeals('earbuds');
        selectedDeal = searchResults[0];
        selectedVariety = selectedDeal.varieties[0].name;
        proceedToConfirm(selectedDeal.id);
      }
      navigateTo(initialView);
    }
  }

  window.addEventListener('hashchange', () => {
    const hash = window.location.hash.replace(/^#/, '');
    if (hash === 'prompt-confirm') {
      navigateTo('ask');
      selectSuggestedPrompt('NovaFit Pro Smart Fitness Tracker Band under $35');
    } else if (hash === 'paypal-sandbox') {
      searchResults = getFallbackDeals('earbuds');
      selectedDeal = searchResults[0];
      selectedVariety = selectedDeal.varieties[0].name;
      proceedToConfirm(selectedDeal.id);
      showPayPalSandboxModal({
        id: 'cw_ord_1794272998876_demo',
        paypalOrderId: '01J588178L663410Y',
        totalAmount: 39.99,
        paypalApproveUrl: 'https://www.sandbox.paypal.com/checkoutnow?token=01J588178L663410Y',
        deal: selectedDeal,
        variety: selectedVariety,
        shippingAddress: { fullName: 'Ada Lovelace', email: 'ada.lovelace@gmail.com' }
      });
    } else if (hash && hash !== currentScreen && ['onboarding', 'ask', 'results', 'confirm', 'agent', 'receipt', 'orders'].includes(hash)) {
      navigateTo(hash);
    }
  });

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

  if (window.location.hash !== `#${screen}` && screen !== 'onboarding') {
    history.pushState(null, '', `#${screen}`);
  } else if (screen === 'onboarding' && window.location.hash) {
    history.pushState(null, '', window.location.pathname);
  }

  if (screen === 'orders') {
    renderOrderHistory();
  } else if (screen === 'ask') {
    updateSearchButtonState();
  } else if (screen === 'results') {
    if (searchResults.length === 0) {
      searchResults = getFallbackDeals('earbuds');
      renderDeals();
    }
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
        btn.className = 'px-2.5 py-2 rounded-xl text-xs font-bold border transition bg-[#0D261C] border-[#0D261C] text-[#F4F2EC] shadow-sm whitespace-nowrap text-center cursor-pointer';
      } else {
        btn.className = 'px-2.5 py-2 rounded-xl text-xs font-medium border transition bg-[#F9F8F5] border-[#E2DFD7] text-[#4A5550] hover:text-[#0D1F18] hover:bg-white whitespace-nowrap text-center cursor-pointer';
      }
    }
  });

  const badge = document.getElementById('active-model-badge');
  if (badge) {
    if (selectedGeminiModel === 'gemini-3.8-flash') badge.textContent = 'Gemini 3.8 Flash (Active)';
    else if (selectedGeminiModel === 'gemini-3.7-flash') badge.textContent = 'Gemini 3.7 Flash (Active)';
    else badge.textContent = 'Gemini 2.5 Flash (Active)';
    badge.className = 'text-[#0F5132] font-bold text-xs uppercase tracking-wider';
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
    if (card) card.className = 'flex items-center justify-between p-3 rounded-xl bg-[#ECFDF5] border border-[#A7F3D0] cursor-pointer transition select-none';
    if (iconWrap) iconWrap.className = 'w-8 h-8 rounded-full bg-[#E6F4EA] flex items-center justify-center transition';
    if (icon) {
      icon.className = 'w-4 h-4 text-[#065F46]';
      icon.setAttribute('data-lucide', 'shield-check');
    }
    if (text) {
      text.textContent = 'Risk score active';
      text.className = 'text-xs text-[#065F46] font-bold mt-0.5';
    }
  } else {
    if (card) card.className = 'flex items-center justify-between p-3 rounded-xl bg-[#F9F8F5] border border-[#E2DFD7] cursor-pointer transition select-none';
    if (iconWrap) iconWrap.className = 'w-8 h-8 rounded-full bg-[#E2DFD7] flex items-center justify-center transition';
    if (icon) {
      icon.className = 'w-4 h-4 text-[#4A5550]';
      icon.setAttribute('data-lucide', 'shield');
    }
    if (text) {
      text.textContent = 'Risk score inactive';
      text.className = 'text-xs text-[#4A5550] font-normal mt-0.5';
    }
  }
  lucide.createIcons();
}

// Search Button Faded State Controller (Fades until text is entered)
function updateSearchButtonState() {
  const input = document.getElementById('search-input');
  const btn = document.getElementById('btn-search-main');
  if (!btn) return;
  const hasText = input && input.value.trim().length > 0;
  if (isSearching) {
    btn.disabled = true;
    btn.className = 'w-full h-[50px] rounded-xl bg-[#0D261C] text-[#F4F2EC] font-bold text-[14px] shadow-sm transition-all flex items-center justify-center gap-2 cursor-wait opacity-80';
  } else if (hasText) {
    btn.disabled = false;
    btn.className = 'w-full h-[50px] rounded-xl bg-[#0D261C] hover:bg-[#16382b] active:bg-[#081711] text-[#F4F2EC] font-bold text-[14px] shadow-sm transition-all flex items-center justify-center gap-2 cursor-pointer opacity-100';
  } else {
    btn.disabled = true;
    btn.className = 'w-full h-[50px] rounded-xl bg-[#E2DFD7] text-[#8C9690] font-bold text-[14px] transition-all flex items-center justify-center gap-2 cursor-not-allowed opacity-60 select-none';
  }
}

// Suggested Search Confirmation Modal (Matching Android AlertDialog)
function selectSuggestedPrompt(prompt) {
  pendingPromptToSearch = prompt;
  const promptEl = document.getElementById('modal-prompt-text');
  if (promptEl) promptEl.textContent = `"${prompt}"`;
  const modal = document.getElementById('modal-prompt-confirm');
  if (modal) modal.classList.remove('hidden');
  if (window.lucide) lucide.createIcons();
}

function closePromptModal() {
  pendingPromptToSearch = null;
  const modal = document.getElementById('modal-prompt-confirm');
  if (modal) modal.classList.add('hidden');
}

function executeModalPrompt() {
  if (pendingPromptToSearch) {
    const prompt = pendingPromptToSearch;
    closePromptModal();
    const input = document.getElementById('search-input');
    if (input) {
      input.value = prompt;
      updateSearchButtonState();
    }
    executeSearch(prompt);
  }
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
  updateSearchButtonState();
  if (label) label.textContent = selectedGeminiModel.includes('3.8') ? 'Gemini 3.8 is searching deals...' : 'Gemini is searching deals...';

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
    updateSearchButtonState();
    if (label) label.textContent = 'Find Best Landed Deal';

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
      <div class="py-12 text-center text-[#4A5550] space-y-2 bg-white rounded-2xl border border-[#E2DFD7] p-6 shadow-sm">
        <i data-lucide="package-search" class="w-10 h-10 mx-auto text-[#8C9690]"></i>
        <div class="text-sm font-bold text-[#0D1F18]">No products found matching "${searchQuery}"</div>
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
      let badgeColor = 'bg-[#ECFDF5] text-[#065F46] border-[#A7F3D0]';
      let levelLabel = 'GOOD TO BUY';
      if (s.level === 'CAUTION') {
        badgeColor = 'bg-[#FEF3C7] text-[#92400E] border-[#FDE68A]';
        levelLabel = 'CAUTION: THIRD-PARTY';
      } else if (s.level === 'RISKY') {
        badgeColor = 'bg-[#FEF2F2] text-[#991B1B] border-[#FECACA]';
        levelLabel = 'RISKY MERCHANT';
      }

      safetyHtml = `
        <div class="p-2.5 rounded-xl bg-[#F9F8F5] border border-[#E2DFD7] space-y-1 mt-2">
          <div class="flex items-center justify-between text-[10px]">
            <span class="font-bold px-2 py-0.5 rounded ${badgeColor} border flex items-center gap-1">
              <i data-lucide="shield" class="w-3 h-3"></i> ${levelLabel} &bull; ${s.score}/100
            </span>
            <span class="text-[#0F5132] font-mono">${s.sslVerified ? '🔒 256-bit SSL' : '⚠️ Unverified'}</span>
          </div>
          <p class="text-[10.5px] text-[#4A5550] leading-tight">${s.summary}</p>
        </div>
      `;
    }

    // Variety pills
    const varietiesHtml = (deal.varieties || []).map(v => {
      const active = v.name === deal.selectedVarietyName;
      let stockText = '';
      if (!v.inStock || v.stockCount <= 0) stockText = '<span class="text-[10px] text-rose-600 font-bold ml-1">(Sold Out)</span>';
      else if (v.stockCount <= 3) stockText = `<span class="text-[10px] text-amber-700 font-bold ml-1">(${v.stockCount} left)</span>`;

      return `
        <button 
          type="button" 
          onclick="selectVariety('${deal.id}', '${v.name.replace(/'/g, "\\'")}')"
          class="px-2 py-1 rounded-lg text-xs font-semibold border transition ${
            active 
              ? 'bg-[#0D261C] border-[#0D261C] text-[#F4F2EC] shadow-sm' 
              : 'bg-[#F9F8F5] border-[#E2DFD7] text-[#4A5550] hover:text-[#0D1F18] hover:bg-white'
          }"
        >
          ${v.name} ${stockText}
        </button>
      `;
    }).join('');

    return `
      <div class="rounded-2xl bg-white border border-[#E2DFD7] p-4 shadow-[0_2px_8px_rgba(13,38,28,0.04)] space-y-3">
        
        <!-- Top Row: Brand & Store Domain Link -->
        <div class="flex items-center justify-between text-xs">
          <span class="font-bold text-[#8C9690] uppercase tracking-wide text-[10px]">${deal.brand || 'Merchant'}</span>
          <a 
            href="${deal.storeUrl || '#'}" 
            target="_blank" 
            class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-[#F9F8F5] border border-[#E2DFD7] text-[#0F5132] hover:text-[#0D261C] font-medium text-[11px] transition"
          >
            <span>${deal.storeName}</span>
            <i data-lucide="external-link" class="w-3 h-3"></i>
          </a>
        </div>

        <!-- Title & Rating -->
        <div>
          <h3 class="text-sm font-bold text-[#0D1F18] line-clamp-2 leading-snug">${deal.title}</h3>
          <div class="flex items-center gap-2 mt-1 text-xs text-[#4A5550]">
            <span class="text-amber-500 font-bold">★ ${deal.rating || '4.8'}</span>
            <span>&bull;</span>
            <span>${deal.reviewsCount || '150'} reviews</span>
            <span>&bull;</span>
            <span class="${isSoldOut ? 'text-rose-600 font-bold' : 'text-[#047857] font-bold'}">
              ${isSoldOut ? 'Sold Out' : (currentVariety ? `${currentVariety.stockCount} in stock` : 'In Stock')}
            </span>
          </div>
        </div>

        <!-- AI Why This Deal Card -->
        <div class="p-2.5 rounded-xl bg-[#E6F4EA] border border-[#B7E1CD] text-xs text-[#0D261C] flex items-start gap-2">
          <i data-lucide="sparkles" class="w-3.5 h-3.5 text-[#0F5132] shrink-0 mt-0.5"></i>
          <span class="leading-relaxed"><strong>Why this deal:</strong> ${deal.reasonWhy || 'Best overall landed cost with verified delivery to your location.'}</span>
        </div>

        <!-- Variety Selector -->
        <div class="space-y-1.5 pt-0.5">
          <div class="text-[10px] font-bold text-[#8C9690] uppercase tracking-wider">Choose Variety:</div>
          <div class="flex flex-wrap gap-1.5">
            ${varietiesHtml}
          </div>
          ${isSoldOut ? `
            <div class="text-xs text-rose-700 font-bold bg-[#FEF2F2] p-2 rounded-lg border border-[#FECACA] mt-1 flex items-center gap-1.5">
              <i data-lucide="alert-circle" class="w-3.5 h-3.5 text-rose-600"></i> Option sold out. Please select another variant.
            </div>
          ` : ''}
        </div>

        ${safetyHtml}

        <!-- Landed Cost & Button -->
        <div class="pt-3 border-t border-[#E2DFD7] space-y-2.5">
          <div class="flex items-baseline justify-between">
            <div>
              <div class="text-[10px] uppercase font-bold text-[#8C9690]">Total Landed Cost</div>
              <div class="text-[11px] text-[#4A5550]">$${deal.price.toFixed(2)} item + $${deal.shippingCost.toFixed(2)} shipping</div>
            </div>
            <div class="text-lg font-extrabold text-[#047857] font-mono">
              $${deal.totalCost.toFixed(2)}
            </div>
          </div>

          <button 
            type="button"
            onclick="proceedToConfirm('${deal.id}')"
            ${isSoldOut ? 'disabled' : ''}
            class="w-full h-[44px] rounded-xl font-bold text-xs transition flex items-center justify-center gap-1.5 ${
              isSoldOut 
                ? 'bg-[#E2DFD7] text-[#8C9690] cursor-not-allowed border border-[#D5D1C8]' 
                : 'bg-[#0D261C] hover:bg-[#16382b] text-[#F4F2EC] shadow-sm cursor-pointer'
            }"
          >
            <span>${isSoldOut ? 'Option Sold Out' : 'Select Deal & Review Escrow'}</span>
            <i data-lucide="arrow-right" class="w-3.5 h-3.5 text-[#A3E635]"></i>
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

// Global PayPal Order State
let currentPendingOrder = null;
let paypalPollInterval = null;

// Trigger Escrow Authorization (Calls backend POST /api/orders and opens PayPal Sandbox)
async function triggerEscrowAuthorization() {
  if (!selectedDeal) return;

  const shipName = document.getElementById('ship-name').value || 'Ada Lovelace';
  const shipPhone = document.getElementById('ship-phone')?.value || '+234 801 234 5678';
  const shipEmail = document.getElementById('ship-email')?.value || 'ada.lovelace@gmail.com';
  const shipAddr = document.getElementById('ship-address').value || '14 Marina Street, Lagos Island';
  const shipCity = document.getElementById('ship-city').value || 'Lagos';
  const shipCountry = document.getElementById('ship-country').value || 'Nigeria';

  const sameAsShipping = document.getElementById('billing-same-check').checked;
  const billName = sameAsShipping ? shipName : (document.getElementById('bill-name').value || shipName);
  const billAddr = sameAsShipping ? shipAddr : (document.getElementById('bill-address').value || shipAddr);

  const shippingData = {
    fullName: shipName,
    phone: shipPhone,
    email: shipEmail,
    address: shipAddr,
    city: shipCity,
    country: shipCountry
  };

  const billingData = { cardholderName: billName, address: billAddr };

  saveShippingAddress(shippingData);

  const authBtn = document.getElementById('btn-authorize-fast');
  const authLabel = document.getElementById('btn-authorize-label');
  const originalLabel = `Authorize Escrow Hold with PayPal ($${selectedDeal.totalCost.toFixed(2)})`;

  if (authBtn) {
    authBtn.disabled = true;
    authBtn.classList.add('opacity-80', 'cursor-not-allowed');
    if (authLabel) {
      authLabel.innerHTML = `<span class="inline-flex items-center gap-2"><i data-lucide="loader-2" class="w-4 h-4 animate-spin text-[#A3E635]"></i> <span>Opening PayPal Sandbox...</span></span>`;
      if (window.lucide) lucide.createIcons();
    }
  }

  try {
    const payload = {
      userId: 'user_default',
      itemTitle: selectedDeal.title,
      itemUrl: selectedDeal.storeUrl,
      itemPrice: selectedDeal.price,
      shippingCost: selectedDeal.shippingCost,
      merchantName: selectedDeal.storeName,
      shippingAddress: shippingData,
      billingAddress: billingData
    };

    const res = await fetch('/api/orders', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (!res.ok) {
      throw new Error(`Order creation failed with status ${res.status}`);
    }

    const orderData = await res.json();
    currentPendingOrder = {
      ...orderData,
      deal: selectedDeal,
      variety: selectedVariety,
      shippingAddress: shippingData,
      billingAddress: billingData
    };

    showPayPalSandboxModal(currentPendingOrder);

  } catch (err) {
    console.warn('[PayPal] API request error, initiating sandbox simulation:', err.message);
    const mockOrderId = `cw_ord_${Date.now()}`;
    const mockPpId = `MOCK-PP-${Math.floor(100000 + Math.random() * 900000)}`;
    currentPendingOrder = {
      id: mockOrderId,
      paypalOrderId: mockPpId,
      paypalApproveUrl: `/api/orders/mock-approve?orderId=${mockOrderId}&paypalOrderId=${mockPpId}`,
      totalAmount: selectedDeal.totalCost,
      status: 'PENDING_APPROVAL',
      deal: selectedDeal,
      variety: selectedVariety,
      shippingAddress: shippingData,
      billingAddress: billingData
    };
    showPayPalSandboxModal(currentPendingOrder);
  } finally {
    if (authBtn) {
      authBtn.disabled = false;
      authBtn.classList.remove('opacity-80', 'cursor-not-allowed');
      if (authLabel) authLabel.textContent = originalLabel;
    }
  }
}

// Display PayPal Sandbox Modal & open sandbox window
function showPayPalSandboxModal(order) {
  const modal = document.getElementById('modal-paypal-sandbox');
  if (!modal) return;

  const orderIdEl = document.getElementById('pp-modal-order-id');
  const ppIdEl = document.getElementById('pp-modal-pp-id');
  const amountEl = document.getElementById('pp-modal-amount');
  const openLinkEl = document.getElementById('pp-modal-open-link');
  const statusNotice = document.getElementById('pp-modal-status-notice');
  const statusText = document.getElementById('pp-modal-status-text');

  if (orderIdEl) orderIdEl.textContent = order.id || 'Pending';
  if (ppIdEl) ppIdEl.textContent = order.paypalOrderId || 'PP-PENDING';
  if (amountEl) amountEl.textContent = `$${(order.totalAmount || selectedDeal?.totalCost || 0).toFixed(2)}`;

  const approveUrl = order.paypalApproveUrl || `https://www.sandbox.paypal.com/checkoutnow?token=${order.paypalOrderId}`;
  if (openLinkEl) {
    openLinkEl.href = approveUrl;
  }

  if (statusNotice) {
    statusNotice.className = 'p-3 rounded-xl bg-[#E6F4EA] border border-[#B7E1CD] text-xs text-[#0D261C] flex items-center gap-2';
  }
  if (statusText) {
    statusText.textContent = 'PayPal Sandbox window opened. Complete authorization or use Fast-Pass below.';
  }

  modal.classList.remove('hidden');
  if (window.lucide) lucide.createIcons();

  // Try to open PayPal Sandbox window in new tab / popup
  try {
    window.open(approveUrl, '_blank');
  } catch (e) {
    console.warn('Popup blocked by browser:', e);
  }

  // Poll for background authorization completion (e.g. if approved in sandbox window)
  if (paypalPollInterval) clearInterval(paypalPollInterval);
  paypalPollInterval = setInterval(async () => {
    if (!currentPendingOrder || !currentPendingOrder.id) return;
    try {
      const checkRes = await fetch(`/api/orders/${currentPendingOrder.id}`);
      if (checkRes.ok) {
        const data = await checkRes.json();
        if (data.order && (data.order.status === 'AUTHORIZED' || data.order.status === 'APPROVED')) {
          clearInterval(paypalPollInterval);
          closePayPalModal();
          startAgentWithOrder({ ...currentPendingOrder, ...data.order });
        }
      }
    } catch (e) {
      // ignore polling errors
    }
  }, 3000);
}

function closePayPalModal() {
  if (paypalPollInterval) {
    clearInterval(paypalPollInterval);
    paypalPollInterval = null;
  }
  const modal = document.getElementById('modal-paypal-sandbox');
  if (modal) modal.classList.add('hidden');
}

async function confirmPayPalApprovalAndStartAgent() {
  if (!currentPendingOrder) return;

  const btn = document.getElementById('pp-modal-confirm-btn');
  const statusNotice = document.getElementById('pp-modal-status-notice');
  const statusText = document.getElementById('pp-modal-status-text');

  if (btn) {
    btn.disabled = true;
    btn.innerHTML = `<span class="inline-flex items-center gap-2"><i data-lucide="loader-2" class="w-4 h-4 animate-spin text-[#A3E635]"></i> <span>Verifying PayPal Authorization...</span></span>`;
    if (window.lucide) lucide.createIcons();
  }

  try {
    const res = await fetch(`/api/orders/${currentPendingOrder.id}/authorize`, {
      method: 'POST'
    });

    if (res.ok) {
      const authorizedOrder = await res.json();
      closePayPalModal();
      startAgentWithOrder({ ...currentPendingOrder, ...authorizedOrder });
      return;
    }

    const errData = await res.json().catch(() => ({}));
    if (errData.error && errData.error.includes('ORDER_NOT_APPROVED')) {
      if (statusNotice) {
        statusNotice.className = 'p-3 rounded-xl bg-[#FEF2F2] border border-[#FECACA] text-xs text-[#991B1B] flex items-center gap-2';
      }
      if (statusText) {
        statusText.innerHTML = 'PayPal reports authorization is still awaiting approval in your sandbox window. Please approve in PayPal, or tap <strong>Demo Sandbox Fast-Pass</strong> below.';
      }
    } else {
      closePayPalModal();
      startAgentWithOrder(currentPendingOrder);
    }
  } catch (err) {
    console.warn('Authorize check error, proceeding in demo mode:', err);
    closePayPalModal();
    startAgentWithOrder(currentPendingOrder);
  } finally {
    if (btn) {
      btn.disabled = false;
      btn.innerHTML = `<i data-lucide="check-circle" class="w-4 h-4 text-[#A3E635]"></i> <span>I\'ve Approved on PayPal &rarr; Start Autonomous Agent</span>`;
      if (window.lucide) lucide.createIcons();
    }
  }
}

function fastPassPayPalAndStartAgent() {
  if (!currentPendingOrder) return;
  closePayPalModal();
  startAgentWithOrder({
    ...currentPendingOrder,
    status: 'AUTHORIZED',
    paypalAuthId: `AUTH-${Math.floor(100000 + Math.random() * 900000)}`
  });
}

function startAgentWithOrder(order) {
  navigateTo('agent');
  executeAgentSimulation({
    deal: order.deal || selectedDeal,
    variety: order.variety || selectedVariety,
    shippingAddress: order.shippingAddress,
    billingAddress: order.billingAddress,
    orderId: order.id,
    paypalOrderId: order.paypalOrderId,
    paypalAuthId: order.paypalAuthId
  });
}

// Agent Simulation Runner
function executeAgentSimulation({ deal, variety, shippingAddress, billingAddress, orderId, paypalOrderId, paypalAuthId }) {
  const isDemo = deal.storeUrl.includes('cartwise-demo-market') || deal.storeName.includes('Demo Market');
  const logsEl = document.getElementById('agent-logs');
  const activeOrderId = orderId || `cw_ord_${Date.now()}`;
  const activePpId = paypalOrderId || `PP-${activeOrderId.slice(-6).toUpperCase()}`;
  const activeAuthId = paypalAuthId || `AUTH-${Math.floor(100000 + Math.random() * 900000)}`;

  logsEl.innerHTML = `<div>[00:01] Orders v2 authorization payload accepted (intent=AUTHORIZE, Order: ${activeOrderId}).</div>`;

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
      stepEl.className = 'p-3 rounded-xl bg-[#E6F4EA] border border-[#0F5132] transition animate-pulse flex items-center gap-3';
      if (iconEl) iconEl.className = 'w-7 h-7 rounded-full bg-[#0D261C] text-[#A3E635] flex items-center justify-center text-xs font-bold shrink-0';
    } else if (state === 'completed') {
      stepEl.className = 'p-3 rounded-xl bg-[#ECFDF5] border border-[#A7F3D0] transition flex items-center gap-3';
      if (iconEl) {
        iconEl.innerHTML = '✓';
        iconEl.className = 'w-7 h-7 rounded-full bg-[#0D261C] text-[#A3E635] flex items-center justify-center text-xs font-bold shrink-0';
      }
    } else if (state === 'failed') {
      stepEl.className = 'p-3 rounded-xl bg-[#FEF2F2] border border-[#FECACA] transition flex items-center gap-3';
      if (iconEl) {
        iconEl.innerHTML = '✕';
        iconEl.className = 'w-7 h-7 rounded-full bg-[#DC2626] text-white flex items-center justify-center text-xs font-bold shrink-0';
      }
    }
  };

  addLog(`PayPal Escrow hold locked ($${deal.totalCost.toFixed(2)}, Auth: ${activeAuthId}). Buyer card NOT charged.`);

  setTimeout(() => {
    setStepState(2, 'active');
    addLog('Booting isolated cloud browser session via Kernel SDK...');
  }, 1100);

  setTimeout(() => {
    setStepState(2, 'completed');
    setStepState(3, 'active');
    addLog(`Navigating to ${deal.storeName}...`);
    addLog(`Selected variety: "${variety}".`);
    addLog(`Guest Checkout: Entering recipient ${shippingAddress.fullName} (Phone: ${shippingAddress.phone || '+234 801 234 5678'}, Email: ${shippingAddress.email || 'ada.lovelace@gmail.com'})...`);
  }, 2400);

  setTimeout(() => {
    setStepState(3, 'completed');
    setStepState(4, 'active');
    addLog('Merchant policy inspection: Checking guest checkout & account requirement...');
    addLog('Merchant password enforced: AI generating and injecting secure merchant account password.');
    addLog('Injecting Cartwise purchasing card: 123456*****2234...');
  }, 3800);

  setTimeout(() => {
    if (isDemo) {
      setStepState(4, 'completed');
      setStepState(5, 'active');
      const dmOrderId = 'DM-' + Math.floor(100000 + Math.random() * 900000);
      addLog('Merchant confirmed purchase! Order ID: ' + dmOrderId);
      addLog('Executing PayPal Capture POST /v2/payments/authorizations/{id}/capture...');

      setTimeout(() => {
        setStepState(5, 'completed');
        addLog('Capture confirmed! Receipt generated with guest recovery credentials.');

        const generatedPassword = 'Cw*' + Math.floor(1000 + Math.random() * 9000) + '!qM26';
        const recoveryUrl = deal.storeUrl ? (deal.storeUrl.includes('cartwise-demo-market') ? 'https://cartwise-demo-market.onrender.com/track' : deal.storeUrl + '/track') : 'https://cartwise-demo-market.onrender.com/track';

        const order = {
          id: activeOrderId,
          merchantOrderId: dmOrderId,
          paypalOrderId: activePpId,
          paypalAuthId: activeAuthId,
          paypalCaptureId: 'CAP-' + Math.floor(100000 + Math.random() * 900000),
          status: 'CAPTURED',
          deal,
          variety,
          totalAmount: deal.totalCost,
          shippingAddress,
          billingAddress,
          guestEmail: shippingAddress.email || 'ada.lovelace@gmail.com',
          guestPhone: shippingAddress.phone || '+234 801 234 5678',
          merchantPasswordRequired: true,
          merchantPassword: generatedPassword,
          merchantRecoveryUrl: recoveryUrl,
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
        addLog(`POST /v2/payments/authorizations/${activeAuthId}/void -> 204 NO CONTENT.`);
        addLog('PayPal escrow released immediately. Buyer charged $0.00.');

        const order = {
          id: activeOrderId,
          paypalOrderId: activePpId,
          paypalAuthId: activeAuthId,
          status: 'VOIDED',
          deal,
          variety,
          totalAmount: 0.00,
          originalAmount: deal.totalCost,
          failureReason: 'Merchant declined test card (Insufficient Funds). PayPal escrow automatically released.',
          shippingAddress,
          billingAddress,
          guestEmail: shippingAddress.email || 'ada.lovelace@gmail.com',
          guestPhone: shippingAddress.phone || '+234 801 234 5678',
          merchantPasswordRequired: false,
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
  const guestEmail = order.guestEmail || order.shippingAddress?.email || 'ada.lovelace@gmail.com';
  const guestPhone = order.guestPhone || order.shippingAddress?.phone || '+234 801 234 5678';
  const merchantPassword = order.merchantPassword || 'Cw*9824!qM26';
  const hasPassword = Boolean(order.merchantPasswordRequired);

  if (isCaptured) {
    container.innerHTML = `
      <div class="text-center space-y-2">
        <div class="w-12 h-12 mx-auto rounded-full bg-[#ECFDF5] border border-[#A7F3D0] flex items-center justify-center text-[#047857]">
          <i data-lucide="check" class="w-6 h-6"></i>
        </div>
        <span class="inline-block px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase bg-[#ECFDF5] text-[#065F46] border border-[#A7F3D0]">
          CAPTURED &bull; PAID
        </span>
        <h2 class="text-xl font-bold text-[#0D1F18]">Order Confirmed!</h2>
        <p class="text-xs text-[#4A5550]">Escrow captured &bull; Merchant receipt secured</p>
      </div>

      <!-- Payment & Transaction Details Card -->
      <div class="bg-[#F9F8F5] rounded-xl p-3.5 border border-[#E2DFD7] space-y-2 text-xs">
        <div class="flex justify-between pb-1.5 border-b border-[#E2DFD7]">
          <span class="text-[#4A5550]">Merchant Order ID</span>
          <span class="font-mono font-bold text-[#0D1F18]">${order.merchantOrderId}</span>
        </div>
        <div class="flex justify-between pb-1.5 border-b border-[#E2DFD7]">
          <span class="text-[#4A5550]">PayPal Capture ID</span>
          <span class="font-mono text-[#0F5132] font-semibold">${order.paypalCaptureId}</span>
        </div>
        <div class="flex justify-between pb-1.5 border-b border-[#E2DFD7]">
          <span class="text-[#4A5550]">Item</span>
          <span class="font-semibold text-[#0D1F18] truncate max-w-[200px]">${order.deal?.title || 'Purchased Item'} (${order.variety})</span>
        </div>
        <div class="flex justify-between pb-1.5 border-b border-[#E2DFD7]">
          <span class="text-[#4A5550]">Recipient</span>
          <span class="text-[#0D1F18]">${order.shippingAddress?.fullName}, ${order.shippingAddress?.city}</span>
        </div>
        <div class="flex justify-between pt-1 font-bold text-sm">
          <span class="text-[#0D1F18]">Amount Paid</span>
          <span class="font-mono text-[#047857]">$${order.totalAmount.toFixed(2)}</span>
        </div>
      </div>

      <!-- Dedicated Merchant Guest Order Recovery Card -->
      <div class="rounded-xl bg-white border border-[#E2DFD7] p-4 space-y-3 text-xs shadow-sm">
        <div class="flex items-center justify-between pb-2 border-b border-[#E2DFD7]">
          <div class="flex items-center gap-2">
            <div class="w-6 h-6 rounded-lg bg-[#E6F4EA] border border-[#B7E1CD] flex items-center justify-center text-[#0F5132]">
              <i data-lucide="key-round" class="w-3.5 h-3.5"></i>
            </div>
            <div>
              <span class="font-bold text-[#0D1F18] text-[12.5px]">Merchant Guest Order Recovery</span>
              <p class="text-[10px] text-[#4A5550]">Credentials needed to manually look up & track on merchant site</p>
            </div>
          </div>
          <span class="px-2 py-0.5 rounded text-[10px] font-extrabold uppercase bg-[#E6F4EA] text-[#0D261C] border border-[#B7E1CD]">
            GUEST CHECKOUT
          </span>
        </div>

        <div class="space-y-2 font-mono text-[11.5px]">
          <div class="flex items-center justify-between py-1 border-b border-[#E2DFD7]">
            <span class="font-sans text-[#4A5550]">Merchant Store</span>
            <span class="font-sans font-semibold text-[#0D1F18]">${order.deal?.storeName || 'Demo Market'}</span>
          </div>

          <div class="flex items-center justify-between py-1 border-b border-[#E2DFD7]">
            <span class="font-sans text-[#4A5550]">Merchant Order ID</span>
            <div class="flex items-center gap-1.5">
              <span class="text-[#0D1F18] font-bold">${order.merchantOrderId}</span>
              <button onclick="navigator.clipboard.writeText('${order.merchantOrderId}'); showToast('Order ID copied');" class="p-1 hover:text-[#0D1F18] text-[#8C9690] cursor-pointer" title="Copy Order ID">
                <i data-lucide="copy" class="w-3 h-3"></i>
              </button>
            </div>
          </div>

          <div class="flex items-center justify-between py-1 border-b border-[#E2DFD7]">
            <span class="font-sans text-[#4A5550]">Guest Email Used</span>
            <div class="flex items-center gap-1.5">
              <span class="text-[#0D1F18]">${guestEmail}</span>
              <button onclick="navigator.clipboard.writeText('${guestEmail}'); showToast('Guest email copied');" class="p-1 hover:text-[#0D1F18] text-[#8C9690] cursor-pointer" title="Copy Email">
                <i data-lucide="copy" class="w-3 h-3"></i>
              </button>
            </div>
          </div>

          <div class="flex items-center justify-between py-1 border-b border-[#E2DFD7]">
            <span class="font-sans text-[#4A5550]">Phone Filled (w/ code)</span>
            <span class="text-[#0D1F18]">${guestPhone}</span>
          </div>

          <!-- Merchant Password with Asterisks and Tap to View -->
          <div class="py-2 border-b border-[#E2DFD7] space-y-1.5">
            <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
              <div class="flex items-center gap-1.5">
                <span class="font-sans text-[#4A5550]">Merchant Password</span>
                <span class="text-[10px] px-1.5 py-0.5 rounded bg-[#E6F4EA] text-[#0D261C] font-sans uppercase font-bold border border-[#B7E1CD]">
                  ${hasPassword ? 'AI ENFORCED' : 'GUEST'}
                </span>
              </div>
              <div class="flex items-center gap-2 flex-wrap">
                <span id="receipt-pass-masked" class="text-[#0D1F18] font-bold tracking-widest text-xs">••••••••••••</span>
                <span id="receipt-pass-plain" class="text-[#047857] font-bold text-xs hidden">${merchantPassword}</span>
                
                <button 
                  onclick="toggleReceiptPassword()" 
                  id="btn-toggle-pass"
                  class="px-2.5 py-1 rounded-lg bg-[#F9F8F5] hover:bg-[#E2DFD7] border border-[#D5D1C8] text-[11px] font-sans font-semibold text-[#0D1F18] inline-flex items-center gap-1 cursor-pointer transition min-h-[30px]"
                >
                  <i data-lucide="eye" class="w-3.5 h-3.5" id="icon-toggle-pass"></i>
                  <span id="text-toggle-pass">Tap to view</span>
                </button>

                <button 
                  onclick="navigator.clipboard.writeText('${merchantPassword}'); showToast('Password copied');"
                  class="p-1.5 hover:text-[#0D1F18] text-[#8C9690] cursor-pointer min-h-[30px] min-w-[30px] flex items-center justify-center" 
                  title="Copy Password"
                >
                  <i data-lucide="copy" class="w-3.5 h-3.5"></i>
                </button>
              </div>
            </div>
            <p class="font-sans text-[11px] text-[#4A5550] leading-tight">
              ${hasPassword 
                ? 'Password automatically created by Cartwise AI because this merchant required account creation during checkout.' 
                : 'Pure guest checkout: Merchant allowed order completion without a mandatory password.'}
            </p>
          </div>
        </div>

        <!-- Recovery Instructions Box -->
        <div class="p-2.5 rounded-lg bg-[#F9F8F5] border border-[#E2DFD7] text-[11px] text-[#4A5550] space-y-1 font-sans">
          <div class="font-bold text-[#0D1F18] flex items-center gap-1.5">
            <i data-lucide="info" class="w-3.5 h-3.5 text-[#0F5132]"></i> How to manually recover or track on merchant store:
          </div>
          <ol class="list-decimal list-inside text-[10.5px] text-[#4A5550] space-y-0.5 pl-1">
            <li>Visit the merchant's <em>"Track Guest Order"</em> or sign-in page.</li>
            <li>Enter your Guest Email (<code class="text-[#0D1F18]">${guestEmail}</code>) and Merchant Order ID (<code class="text-[#0D1F18]">${order.merchantOrderId}</code>).</li>
            <li>When asked for password, use the AI-generated password revealed above.</li>
          </ol>
        </div>

        <a 
          href="${order.merchantRecoveryUrl || (order.deal?.storeUrl ? order.deal.storeUrl + '/track' : 'https://cartwise-demo-market.onrender.com/track')}" 
          target="_blank" 
          rel="noopener noreferrer" 
          class="w-full py-2.5 rounded-xl bg-white hover:bg-[#F4F2EC] border border-[#D5D1C8] text-[#0D1F18] font-bold text-xs transition flex items-center justify-center gap-2 cursor-pointer"
        >
          <i data-lucide="external-link" class="w-3.5 h-3.5 text-[#0F5132]"></i>
          <span>Open Merchant Order Lookup ↗</span>
        </a>
      </div>

      <div class="space-y-2 pt-1">
        <button onclick="navigateTo('orders')" class="w-full h-[46px] rounded-xl bg-[#0D261C] hover:bg-[#16382b] text-[#F4F2EC] font-bold text-xs shadow-sm transition flex items-center justify-center gap-1.5 cursor-pointer">
          <i data-lucide="history" class="w-4 h-4 text-[#A3E635]"></i> View in Order History
        </button>
        <button onclick="navigateTo('ask')" class="w-full h-[40px] rounded-xl bg-white hover:bg-[#F4F2EC] text-[#0D1F18] font-semibold text-xs border border-[#D5D1C8] transition cursor-pointer">
          Shop Again
        </button>
      </div>
    `;
  } else {
    container.innerHTML = `
      <div class="text-center space-y-2">
        <div class="w-12 h-12 mx-auto rounded-full bg-[#FEF2F2] border border-[#FECACA] flex items-center justify-center text-[#DC2626]">
          <i data-lucide="shield-alert" class="w-6 h-6"></i>
        </div>
        <span class="inline-block px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase bg-[#FEF2F2] text-[#991B1B] border border-[#FECACA]">
          VOIDED &bull; $0.00 CHARGED
        </span>
        <h2 class="text-xl font-bold text-[#0D1F18]">Escrow Hold Released!</h2>
        <p class="text-xs text-[#4A5550]">Autonomous Guardrail Activated</p>
      </div>

      <div class="p-3 rounded-xl bg-[#FEF2F2] border border-[#FECACA] text-xs text-[#991B1B] leading-relaxed">
        <strong>Guardrail Protected:</strong> Merchant declined test card (Insufficient Funds). Cartwise automatically voided your PayPal authorization. <strong>You were charged $0.00.</strong>
      </div>

      <div class="bg-[#F9F8F5] rounded-xl p-3.5 border border-[#E2DFD7] space-y-2 text-xs">
        <div class="flex justify-between pb-1.5 border-b border-[#E2DFD7]">
          <span class="text-[#4A5550]">PayPal Auth ID</span>
          <span class="font-mono font-bold text-[#0D1F18]">${order.paypalAuthId}</span>
        </div>
        <div class="flex justify-between pb-1.5 border-b border-[#E2DFD7]">
          <span class="text-[#4A5550]">Guest Checkout Contact</span>
          <span class="text-[#0D1F18]">${guestPhone} &bull; ${guestEmail}</span>
        </div>
        <div class="flex justify-between pb-1.5 border-b border-[#E2DFD7]">
          <span class="text-[#4A5550]">Status</span>
          <span class="font-bold text-amber-700">RELEASED / VOIDED</span>
        </div>
        <div class="flex justify-between pt-1 font-bold text-sm">
          <span class="text-[#0D1F18]">Amount Charged</span>
          <span class="font-mono text-[#047857]">$0.00</span>
        </div>
      </div>

      <div class="space-y-2 pt-1">
        <button onclick="navigateTo('orders')" class="w-full h-[46px] rounded-xl bg-white hover:bg-[#F4F2EC] text-[#0D1F18] font-bold text-xs border border-[#D5D1C8] transition flex items-center justify-center gap-1.5 cursor-pointer">
          <i data-lucide="history" class="w-4 h-4 text-[#0F5132]"></i> View in Order History
        </button>
        <button onclick="navigateTo('ask')" class="w-full h-[40px] rounded-xl bg-[#0D261C] hover:bg-[#16382b] text-[#F4F2EC] font-semibold text-xs transition cursor-pointer">
          Shop Again
        </button>
      </div>
    `;
  }
  lucide.createIcons();
}

function toggleReceiptPassword() {
  const masked = document.getElementById('receipt-pass-masked');
  const plain = document.getElementById('receipt-pass-plain');
  const textBtn = document.getElementById('text-toggle-pass');

  if (!masked || !plain) return;

  const isMasked = !masked.classList.contains('hidden');
  if (isMasked) {
    masked.classList.add('hidden');
    plain.classList.remove('hidden');
    if (textBtn) textBtn.textContent = 'Hide';
  } else {
    masked.classList.remove('hidden');
    plain.classList.add('hidden');
    if (textBtn) textBtn.textContent = 'Tap to view';
  }
}

function showToast(message) {
  let toast = document.getElementById('cw-toast');
  if (!toast) {
    toast = document.createElement('div');
    toast.id = 'cw-toast';
    toast.className = 'fixed bottom-6 right-6 z-50 bg-[#0D261C] text-[#F4F2EC] border border-[#0D261C] px-3.5 py-2 rounded-xl text-xs shadow-xl transition-all duration-300 opacity-0 pointer-events-none font-medium';
    document.body.appendChild(toast);
  }
  toast.textContent = message;
  toast.classList.remove('opacity-0', 'pointer-events-none');
  setTimeout(() => {
    toast.classList.add('opacity-0', 'pointer-events-none');
  }, 2200);
}

function viewReceiptForOrder(orderId) {
  const order = orderHistory.find(o => o.id === orderId);
  if (order) {
    renderReceiptScreen(order);
    navigateTo('receipt');
  }
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
      <div class="py-12 text-center text-[#4A5550] space-y-2 bg-white rounded-2xl border border-[#E2DFD7] p-6 shadow-sm">
        <i data-lucide="clipboard-list" class="w-10 h-10 mx-auto text-[#8C9690]"></i>
        <div class="text-sm font-bold text-[#0D1F18]">No Orders Recorded Yet</div>
        <p class="text-xs">Completed purchases and protected escrow releases will appear here.</p>
        <button onclick="navigateTo('ask')" class="px-3.5 py-2 rounded-xl bg-[#0D261C] hover:bg-[#16382b] text-[#F4F2EC] font-bold text-xs transition mt-2 cursor-pointer shadow-sm">
          Start Shopping
        </button>
      </div>
    `;
    lucide.createIcons();
    return;
  }

  container.innerHTML = orderHistory.map(o => {
    const isCaptured = o.status === 'CAPTURED';
    const guestPhone = o.guestPhone || o.shippingAddress?.phone || '+234 801 234 5678';
    const guestEmail = o.guestEmail || o.shippingAddress?.email || 'ada.lovelace@gmail.com';

    return `
      <div onclick="viewReceiptForOrder('${o.id}')" class="rounded-2xl bg-white border border-[#E2DFD7] hover:border-[#0D261C] p-4 shadow-[0_2px_8px_rgba(13,38,28,0.04)] space-y-2.5 cursor-pointer transition group">
        <div class="flex items-center justify-between pb-2 border-b border-[#E2DFD7]">
          <div class="flex items-center gap-2">
            <span class="px-2 py-0.5 rounded text-[10px] font-bold uppercase ${
              isCaptured ? 'bg-[#ECFDF5] text-[#065F46] border border-[#A7F3D0]' : 'bg-[#FEF2F2] text-[#991B1B] border border-[#FECACA]'
            }">
              ${isCaptured ? 'CAPTURED • PAID' : 'VOIDED • $0 CHARGED'}
            </span>
            <span class="px-2 py-0.5 rounded text-[10px] font-bold uppercase bg-[#E6F4EA] text-[#0D261C] border border-[#B7E1CD]">
              GUEST ORDER
            </span>
          </div>
          <span class="text-xs font-mono font-bold ${isCaptured ? 'text-[#047857]' : 'text-[#4A5550]'}">
            $${o.totalAmount.toFixed(2)}
          </span>
        </div>

        <div>
          <h4 class="text-xs font-bold text-[#0D1F18] group-hover:text-[#0F5132] transition line-clamp-1">${o.deal?.title || 'Purchased Item'}</h4>
          <div class="text-[11px] text-[#4A5550] mt-0.5 flex flex-wrap items-center gap-1.5">
            <span class="px-1.5 py-0.2 rounded bg-[#F9F8F5] text-[#0D1F18] font-semibold border border-[#E2DFD7]">${o.variety || 'Default'}</span>
            <span>&bull;</span>
            <span class="text-[#0F5132] font-medium">${o.deal?.storeName || 'Merchant'}</span>
            <span>&bull;</span>
            <span class="text-[#8C9690]">${o.timestamp || ''}</span>
          </div>
        </div>

        <div class="pt-2 text-[11px] text-[#4A5550] flex flex-col sm:flex-row sm:items-center justify-between gap-1 border-t border-[#E2DFD7] font-mono">
          <div class="truncate text-[#4A5550] text-[10.5px]">
            Guest: <span class="text-[#0D1F18] font-medium">${o.shippingAddress?.fullName || 'Recipient'}</span> &bull; ${guestPhone}
          </div>
          <div class="flex items-center gap-2">
            <span class="text-[#0F5132] text-[10.5px] font-semibold">${isCaptured ? o.merchantOrderId : o.paypalAuthId}</span>
            <span class="text-[#4A5550] text-[10px] group-hover:text-[#0D1F18] font-sans font-medium">View Receipt ↗</span>
          </div>
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
      if (addr.phone && document.getElementById('ship-phone')) document.getElementById('ship-phone').value = addr.phone;
      if (addr.email && document.getElementById('ship-email')) document.getElementById('ship-email').value = addr.email;
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
      reasonWhy: "Best value pick: lowest total landed cost ($39.99) with verified shipping to your location.",
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
