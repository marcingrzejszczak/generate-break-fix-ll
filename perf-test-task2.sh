#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "Running k6 load test (TASK2 /place-order): 100 req/s for 30s"
echo "---"

docker run --rm --network=host \
  -v "$SCRIPT_DIR/perf-test-task2.js:/scripts/perf-test-task2.js:ro" \
  grafana/k6 run /scripts/perf-test-task2.js
