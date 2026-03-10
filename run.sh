#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

cleanup() {
  echo ""
  echo "Shutting down..."
  kill "$PAYMENT_PID" "$ORDER_PID" 2>/dev/null || true
  docker compose -f "$SCRIPT_DIR/docker-compose.yml" down 2>/dev/null || true
  echo "Done."
}
trap cleanup EXIT

# 1. Start Payment Service (TypeScript, port 8080)
echo "==> Starting Payment Service on port 8080..."
node "$SCRIPT_DIR/payment-service/server.js" &
PAYMENT_PID=$!
sleep 1

# 2. Start Toxiproxy (port 9753 -> 8080)
echo "==> Starting Toxiproxy..."
docker compose -f "$SCRIPT_DIR/docker-compose.yml" up -d
sleep 2

# 3. Build and start Order Service (Java, port 3456)
echo "==> Building Order Service..."
cd "$SCRIPT_DIR/order-service"
./mvnw -q package -DskipTests
echo "==> Starting Order Service on port 3456..."
java -jar target/*.jar &
ORDER_PID=$!
cd "$SCRIPT_DIR"

echo ""
echo "==> Waiting for Order Service to be ready..."
until curl -sf http://localhost:3456/actuator/health > /dev/null 2>&1; do
  sleep 1
done

echo ""
echo "============================================"
echo "  All services are running!"
echo "  Order Service:   http://localhost:3456"
echo "  Payment Service: http://localhost:8080"
echo "  Proxy:           http://localhost:9753"
echo "============================================"
echo ""
echo "Test with:"
echo '  curl -s -X POST http://localhost:3456/orders -H "Content-Type: application/json" -d '\''{"productId":"book-123","amount":49.99}'\'' | jq .'
echo ""
echo "Press Ctrl+C to stop all services."
wait
