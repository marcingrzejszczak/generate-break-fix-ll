#!/usr/bin/env bash
set -euo pipefail

URL="http://host.docker.internal:3456/orders"
CONCURRENCY=10
REQUESTS=100

echo "Running hey via Docker: $REQUESTS requests, $CONCURRENCY concurrent"
echo "Target: $URL"
echo "---"

docker run --rm --add-host=host.docker.internal:host-gateway \
  williamyeh/hey \
  -n "$REQUESTS" \
  -c "$CONCURRENCY" \
  -m POST \
  -H "Content-Type: application/json" \
  -d '{"productId": "book-123", "amount": 49.99}' \
  "$URL"
