#!/usr/bin/env bash
set -euo pipefail

echo "Removing all toxics from service-a proxy..."

toxics=$(curl -sf http://localhost:8474/proxies/service-a | jq -r '.toxics[].name // empty')

if [ -z "$toxics" ]; then
  echo "No toxics found."
  exit 0
fi

for toxic in $toxics; do
  echo "  Removing toxic: $toxic"
  curl -sf -X DELETE "http://localhost:8474/proxies/service-a/toxics/$toxic" > /dev/null
done

echo "Done. All toxics removed."
