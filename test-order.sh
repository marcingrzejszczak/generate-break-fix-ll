#!/usr/bin/env bash
set -euo pipefail

echo "Placing an order..."
curl -s -X POST http://localhost:3456/orders \
  -H "Content-Type: application/json" \
  -d '{"productId":"book-123","amount":49.99}' | jq .
