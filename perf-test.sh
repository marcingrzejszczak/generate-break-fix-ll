#!/usr/bin/env bash
set -euo pipefail

URL="http://localhost:9753"
CONCURRENCY=10

echo "Sending $CONCURRENCY concurrent requests to $URL ..."
echo "---"

start=$(date +%s%N)

pids=()
for i in $(seq 1 $CONCURRENCY); do
  (
    req_start=$(date +%s%N)
    status=$(curl -s -o /dev/null -w "%{http_code}" --max-time 30 "$URL" 2>&1) || status="FAIL"
    req_end=$(date +%s%N)
    elapsed=$(( (req_end - req_start) / 1000000 ))
    echo "  Request $i: status=$status  time=${elapsed}ms"
  ) &
  pids+=($!)
done

for pid in "${pids[@]}"; do
  wait "$pid" 2>/dev/null || true
done

end=$(date +%s%N)
total=$(( (end - start) / 1000000 ))

echo "---"
echo "All $CONCURRENCY requests completed in ${total}ms total wall time."
