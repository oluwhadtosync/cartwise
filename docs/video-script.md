# Cartwise Demo Video Script (Duration: 2 minutes 45 seconds)

**Story:** Ada in Lagos wants wireless noise-cancelling earbuds. Her favourite online store doesn't take PayPal, and international shipping can be unpredictable. She asks Cartwise, gets 3 top deals, approves safely with PayPal, watches the autonomous agent buy it live in the cloud browser, and receives her merchant receipt. Then, a quick demonstration of the failure guardrail proves she is never charged if an item sells out.

---

### **[0:00 - 0:25] ACT 1: The Problem & The Solution**
* **Visual:** Close-up of Ada on her Android phone in Lagos. Screen shows a regular merchant checkout page displaying: *"Payment Methods: Credit/Debit Cards only. (No PayPal accepted)"*.
* **Voiceover (Ada):**
  > "I live in Lagos, Nigeria. When I shop internationally, PayPal is the only payment method I trust with my money. But so many incredible stores simply don't take PayPal. Comparing shipping costs across five websites takes forever, and sharing my local bank card with foreign websites is risky."
* **Visual:** Ada opens the **Cartwise** Android app. Clean Material 3 interface with brand badge and a voice input prompt.
* **Voiceover:**
  > "That's why we created Cartwise — an AI shopping agent that finds the cheapest real deal and buys for you, paid safely with PayPal."

---

### **[0:25 - 0:50] ACT 2: Natural Language Search & AI Deal Ranking**
* **Visual:** Ada speaks into the Cartwise app: *"Find me wireless noise-cancelling earbuds under $40 that ship to Lagos."*
* **Visual:** Animated searching state showing Gemini query extraction $\rightarrow$ Channel3 multi-store API query.
* **Visual:** Results screen populates with Top 3 Deal Cards:
  1. **SoundPulse Air Wireless Earbuds** - $34.99 + $5.00 shipping to Lagos (Total: $39.99). Store: Demo Market.
     - *Badge:* "Top Pick: Lowest Landed Cost & High Reliability".
  2. NovaAudio Sport - $38.00 + $8.50 shipping (Total: $46.50).
  3. BaseSound Pro - $29.99 + $18.00 shipping (Total: $47.99).
* **Voiceover:**
  > "Cartwise's Google Gemini engine extracts my constraints, searches across stores via Channel3, and calculates the true landed cost — including verified shipping to Nigeria. Look at option 3: the sticker price was lower, but shipping was $18! Gemini ranks the SoundPulse Air as the smartest deal."

---

### **[0:50 - 1:20] ACT 3: PayPal Escrow Authorization (Intent: AUTHORIZE)**
* **Visual:** Ada taps on Deal #1. Price breakdown card appears:
  - Item Price: $34.99
  - Shipping to Lagos: $5.00
  - Cartwise Service Fee: $3.00
  - **Total Order Hold:** $42.99
* **Visual:** Ada taps the prominent **"Pay with PayPal"** button. A Chrome Custom Tab opens PayPal Sandbox checkout.
* **Voiceover:**
  > "Here is the key innovation: when I tap 'Pay with PayPal', Cartwise uses PayPal Orders v2 with intent AUTHORIZE. My money is held safely in escrow. Not a single cent is captured until the store actually confirms my order."
* **Visual:** Ada logs in to PayPal sandbox, approves the authorization, and is redirected seamlessly back into Cartwise via deep-link (`cartwise://paypal/return`).

---

### **[1:20 - 1:55] ACT 4: Autonomous Checkout Agent Live Stream (Kernel + Playwright)**
* **Visual:** The screen transitions to the **Agent at Work** view.
  - Top half: Embedded WebView showing **Kernel's Live View** cloud browser in real time.
  - Bottom half: Step-by-step interactive timeline ("1. Opening Demo Market store...", "2. Selecting Matte Black variant...", "3. Adding to cart...", "4. Filling shipping to Lagos...").
* **Voiceover:**
  > "Now watch this. Behind the scenes, Cartwise boots an isolated Kernel cloud browser. Running Playwright and Gemini, the agent navigates the store, selects my preferred variant, enters my Lagos shipping address, and injects Cartwise's commercial purchasing card. My personal banking details are never exposed."
* **Visual:** The agent reaches the final review page. Cartwise displays a screenshot preview of the cart total: $39.99 $\le$ $42.99 authorized. Ada taps **"Confirm & Place Order"**.
* **Visual:** Playwright clicks "Place Order". The store responds with order confirmation `#DM-849201`.

---

### **[1:55 - 2:20] ACT 5: PayPal Capture & Instant Receipt**
* **Visual:** The Cartwise screen instantly transitions to the **Order Receipt** screen with confetti animation.
  - Merchant Order ID: `DM-849201`
  - Store: Demo Market (Credit Card Only Store)
  - PayPal Capture ID: `CAP-938210384`
  - Total Paid: $42.99
  - Status: "Payment Captured & Order Confirmed"
* **Voiceover:**
  > "The moment Demo Market confirmed the purchase, Cartwise executed the PayPal capture. I bought from a store that doesn't accept PayPal, without risking my card, and with full PayPal buyer protection."

---

### **[2:20 - 2:45] ACT 6: The Guardrail — Zero Risk on Stockout (Void Flow)**
* **Visual:** Quick split-screen. Ada attempts to purchase an item that just went out of stock during checkout.
* **Visual:** The agent detects the out-of-stock badge on the merchant cart page.
* **Visual:** Instead of capturing, Cartwise immediately executes `POST /v2/payments/authorizations/{id}/void`.
* **Visual:** The app alerts:
  - Banner: *"Item Out of Stock &bull; PayPal Authorization Voided"*
  - Subtitle: *"You were NOT charged. Held funds have been released back to your PayPal balance."*
* **Voiceover:**
  > "And if an item sells out while the agent is checking out? Cartwise immediately voids the PayPal authorization. You are never left waiting for refunds or customer support."
* **Visual:** Ada smiles, looking at her order history. Cartwise logo + PayPal badge + GitHub repository link on screen.
* **Voiceover:**
  > "Cartwise: True agentic commerce, backed by the safety of PayPal. Thank you!"
