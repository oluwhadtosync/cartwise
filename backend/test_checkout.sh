#!/bin/bash
set -e

# Start Demo Market on 4000
node /root/cartwise/demo-store/src/server.js > /tmp/demo.log 2>&1 & DEMO_PID=$!
echo "Demo Market PID: $DEMO_PID"

# Start Backend on 3000
node /root/cartwise/backend/dist/server.js > /tmp/backend.log 2>&1 & BACKEND_PID=$!
echo "Backend PID: $BACKEND_PID"

# Wait for both services to be ready
echo "Waiting for services to start..."
while ! curl -s http://localhost:4000/health > /dev/null; do
  sleep 0.5
done
echo "Demo Market is ready."

while ! curl -s http://localhost:3000/health > /dev/null; do
  sleep 0.5
done
echo "Backend is ready."

# 1. Create Order
echo ""
echo "=== 1. CREATING ORDER ==="
CREATE_RES=$(curl -s -X POST http://localhost:3000/api/orders \
  -H "Content-Type: application/json" \
  -d '{"itemTitle": "SoundPulse Air Wireless Earbuds", "itemUrl": "http://localhost:4000/product/prod-earbuds-01", "itemPrice": 34.99, "shippingCost": 5.00}')
echo "Response: $CREATE_RES"
ORDER_ID=$(python3 -c "import json; print(json.loads('''$CREATE_RES''')['id'])")
echo "Extracted Order ID: $ORDER_ID"

# 2. Authorize via PayPal
echo ""
echo "=== 2. AUTHORIZING PAYMENT HOLD VIA PAYPAL ==="
AUTH_RES=$(curl -s -X POST "http://localhost:3000/api/orders/$ORDER_ID/authorize")
echo "Authorized: $AUTH_RES"

# 3. Start Autonomous Checkout Agent
echo ""
echo "=== 3. STARTING CHECKOUT AGENT ==="
START_RES=$(curl -s -X POST "http://localhost:3000/api/checkout/$ORDER_ID/start")
echo "Agent Start: $START_RES"

# 4. Wait for agent to finish steps
echo "Waiting for agent to navigate, verify stock, inject card, verify price, and capture..."
sleep 10

# 5. Check Final Order & Timeline
echo ""
echo "=== 4. FINAL ORDER STATUS & TIMELINE ==="
curl -s "http://localhost:3000/api/checkout/$ORDER_ID/status" | python3 -m json.tool

# Cleanup
kill $DEMO_PID $BACKEND_PID 2>/dev/null || true
echo ""
echo "=== TEST COMPLETED SUCCESSFULLY ==="
