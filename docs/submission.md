# Cartwise: Autonomous AI Shopping Agent Powered by PayPal

> **Devpost Submission Draft for the PayPal AI Hackathon 2026**
> *Find the best real deal across the web, approve securely with PayPal, and let your AI agent buy for you — even from stores that don't accept PayPal.*

---

## 💡 Inspiration
Cross-border and online shoppers — particularly in emerging markets like Nigeria, Ghana, Kenya, and across the globe — face a frustrating barrier: **PayPal is their most trusted and secure payment method, but thousands of independent merchants and regional stores simply do not accept PayPal.**

Shoppers spend hours manually comparing prices across dozens of e-commerce sites, calculating volatile international shipping costs, and worrying about whether foreign merchants will accept their local payment cards or compromise their banking information.

We asked: **What if an AI shopping agent could search the web for the best price, calculate true landed cost including shipping, and autonomously execute the purchase on the user's behalf — while the user pays 100% safely through PayPal?**

That vision is **Cartwise**.

---

## 🛒 What It Does
**Cartwise** is an autonomous AI commerce concierge and native Android application:
1. **Natural Language Search:** The user speaks or types what they need (e.g., *"Find me wireless noise-cancelling earbuds under $40 that ship to Lagos"*).
2. **AI Intent & Multi-Store Search:** Google Gemini extracts structured search constraints (item, budget, destination, features), queries the **Channel3** multi-store product API, and evaluates real-time offers across merchants.
3. **AI Deal Scoring & Landed Cost:** Gemini ranks options based on total landed cost (item + shipping to Nigeria), merchant trust, and customer reviews, presenting the top 3 best deals with a clear explanation of *why* each was chosen.
4. **PayPal Authorization Escrow (Orders v2):** The shopper taps **Pay with PayPal** to approve the total order amount using PayPal Orders v2 with `intent=AUTHORIZE`. **Money is held, not charged yet.**
5. **Autonomous Cloud Browser Agent (Kernel + Playwright):** Cartwise provisions an isolated cloud browser via **Kernel** and runs an intelligent Playwright agent. The user watches a real-time Live View directly inside the Android app as the agent navigates the store, selects variants, fills shipping details, and injects Cartwise's commercial purchasing card.
6. **Guardrails & Dual-Approval:** 
   - Strict cart total verification: The agent aborts if the cart exceeds the authorized PayPal amount.
   - Dry-run mode for live merchant inspection.
7. **Two Paths to Total Trust:**
   - **Happy Path:** The store confirms the order $\rightarrow$ Cartwise **captures** the PayPal authorization $\rightarrow$ Instant receipt with merchant order ID and PayPal capture ID.
   - **Failure / Out-of-Stock Guardrail:** If an item sells out or checkout fails $\rightarrow$ Cartwise automatically **voids** the PayPal authorization $\rightarrow$ The hold is immediately released and the user sees *"You were not charged."*

---

## 🛠️ How We Built It

### 1. PayPal Developer Platform (Sandbox)
PayPal is the beating heart of Cartwise's security and payment architecture:
- **Orders v2 API (`intent=AUTHORIZE`):** Creates the payment hold for item price + shipping + Cartwise service fee ($3.00), returning approval links for the Android Chrome Custom Tab flow.
- **Payments v2 API (Capture):** Executes `POST /v2/payments/authorizations/{id}/capture` when the merchant checkout is successfully confirmed.
- **Payments v2 API (Void):** Executes `POST /v2/payments/authorizations/{id}/void` if merchant stock drops or checkout encounters errors, ensuring zero financial risk to the shopper.
- **Webhooks v1:** Real-time event ingestion (`CHECKOUT.ORDER.APPROVED`, `PAYMENT.AUTHORIZATION.CREATED`, `PAYMENT.AUTHORIZATION.VOIDED`, `PAYMENT.CAPTURE.COMPLETED`) with signature verification.

### 2. Artificial Intelligence & Multi-Store Search
- **Google Gemini API (`@google/genai`):** Used for natural language intent extraction, semantic product comparison, landed cost ranking, and vision-based DOM checkout decision making.
- **Channel3 API:** Powers real-time product and pricing queries across hundreds of online retailers with sub-second response times.
- **Kernel (`@onkernel/sdk`):** Cloud browser infrastructure providing isolated sessions and interactive CDP live stream endpoints directly visible in the Android app.

### 3. Native Android Mobile App
- **Jetpack Compose + Material 3:** Modern, reactive UI supporting Dark Mode and accessible design.
- **MVVM Architecture:** Clean separation of concerns with StateFlow, Retrofit, and Coroutines.
- **Live Stream WebView & SSE:** Real-time Server-Sent Events timeline synchronized with Kernel's interactive browser viewport.

### 4. Demo Market
A simulated standalone e-commerce merchant hosted on Render that exclusively accepts credit/debit cards (no PayPal), with dynamic stockout controls so hackathon judges can verify both the happy path and the void-on-failure path end-to-end.

---

## 🛡️ Security & Privacy Guardrails
- **Zero Card Exposure to LLMs:** Commercial payment card details are stored securely on the backend and injected into forms via Playwright. Card credentials are never included in prompts, logs, or LLM contexts.
- **Landed Cost Bound:** The agent checks the final checkout total before clicking "Place Order". If the merchant total exceeds the user's PayPal authorization, execution halts immediately.
- **User Confirmation:** Explicit approval required before PayPal authorization and before final purchase execution.

---

## 🚀 What's Next for Cartwise
- **Multi-Merchant Cart Splitting:** Buying bundles from 3 different stores in a single PayPal authorization.
- **Automated Customs & Duty Calculator:** Instant CIF calculations for West African and Latin American imports.
- **PayPal Agent Toolkit MCP Integration:** Enabling external conversational assistants to invoke Cartwise as an autonomous purchasing skill.

---

## 🔗 Links & Judge Evaluation Resources

- **Public GitHub Repository:** [https://github.com/oluwhadtosync/cartwise](https://github.com/oluwhadtosync/cartwise)
- **Live Demo Store:** [https://cartwise-demo-market.onrender.com](https://cartwise-demo-market.onrender.com)
- **Live Backend API:** [https://cartwise-backend-kr8p.onrender.com](https://cartwise-backend-kr8p.onrender.com)
- **Backend Health Check:** [https://cartwise-backend-kr8p.onrender.com/health](https://cartwise-backend-kr8p.onrender.com/health)
- **Pre-compiled Android APK:** `android/app/build/outputs/apk/debug/app-debug.apk` (19 MB)

### 🔑 Test Judge Credentials (PayPal Sandbox)
- **Buyer Email:** `sb-buyer@business.example.com`
- **Buyer Password:** `CartwiseDemo2026!`
- **Demo Store Card:** `4242 4242 4242 4242` (Expiry: `12/28`, CVC: `123`)
