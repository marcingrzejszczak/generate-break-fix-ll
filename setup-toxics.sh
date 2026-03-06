#!/usr/bin/env bash
set -euo pipefail

echo "Waiting for Toxiproxy API..."
until curl -sf http://localhost:8474/version > /dev/null 2>&1; do
  sleep 0.5
done

echo "Adding 5s latency toxic to service-a proxy..."
curl -sf -X POST http://localhost:8474/proxies/service-a/toxics \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "latency_downstream",
    "type": "latency",
    "stream": "downstream",
    "attributes": { "latency": 5000, "jitter": 0 }
  }' | jq .

echo "Toxic added successfully."
