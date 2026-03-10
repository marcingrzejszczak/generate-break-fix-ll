#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "Running k6 load test: 100 req/s for 30s"
echo "---"

docker run --rm --network=host \
  -v "$SCRIPT_DIR/perf-test.js:/scripts/perf-test.js:ro" \
  grafana/k6 run /scripts/perf-test.js
