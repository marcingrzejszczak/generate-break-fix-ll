#!/usr/bin/env bash
set -euo pipefail

URL="http://host.docker.internal:3456/orders"
CONCURRENCY=100
DURATION=30
RPS=100

echo "Running hey via Docker: ${DURATION}s, ${RPS} req/s, $CONCURRENCY concurrent"
echo "Target: $URL"
echo "---"

docker run --rm --add-host=host.docker.internal:host-gateway \
  williamyeh/hey \
  -z "${DURATION}s" \
  -q "$RPS" \
  -c "$CONCURRENCY" \
  -m POST \
  -H "Content-Type: application/json" \
  -d '{"productId": "book-123", "amount": 49.99}' \
  "$URL"
