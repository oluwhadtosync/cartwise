#!/bin/bash
set -e

# Start Demo Market on 4000
node /root/cartwise/demo-store/src/server.js > /tmp/demo.log 2>&1 & DEMO_PID=$!
# Start Backend on 3000
node /root/cartwise/backend/dist/server.js > /tmp/backend.log 2>&1 & BACKEND_PID=$!

while ! curl -s http://localhost:4000/health > /dev/null; do sleep 0.5; done
while ! curl -s http://localhost:3000/health > /dev/null; do sleep 0.5; done

echo "=== 1. CREATING ORDER FOR OUT-OF-STOCK ITEM (prod-keyboard-04) ==="
CREATE_RES=$(curl -s -X POST http://localhost:3000/api/orders \
  -H "Content-Type: application/json" \
  -d '{"itemTitle": "SwiftType Mechanical Keyboard", "itemUrl": "http://localhost:4000/product/prod-keyboard-04", "itemPrice": 45.00, "shippingCost": 6.00}')
ORDER_ID=$(python3 -c "import json; print(json.loads('''$CREATE_RES''')['id'])")
echo "Created Order: $ORDER_ID"

echo "=== 2. AUTHORIZING PAYMENT HOLD ==="
curl -s -X POST "http://localhost:3000/api/orders/$ORDER_ID/authorize" > /dev/null
echo "Authorized!"

echo "=== 3. STARTING AGENT CHECKOUT ==="
curl -s -X POST "http://localhost:3000/api/checkout/$ORDER_ID/start" > /dev/null

echo "Waiting for agent to detect stockout and trigger automatic VOID..."
sleep 5

echo "=== 4. CHECK FINAL STATUS (SHOULD BE FAILED/VOIDED) ==="
curl -s "http://localhost:3000/api/checkout/$ORDER_ID/status" | python3 -m json.tool

kill $DEMO_PID $BACKEND_PID 2>/dev/null || true
