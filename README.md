# Lightning Session

Two services that communicate with each other through a Toxiproxy-managed network, allowing simulation of adverse network conditions like latency.

## Architecture

```
Client -> :9753 (Toxiproxy + 5s latency) -> :8080 (Service A) -> Service B
```

Toxiproxy sits between the client and Service A, introducing a configurable 5-second latency on all responses.

## Prerequisites

- Docker & Docker Compose
- `curl` and `jq` (for setup and test scripts)

## Quick Start

1. Start Toxiproxy:
   ```bash
   docker compose up -d
   ```

2. Add the 5-second latency toxic:
   ```bash
   ./setup-toxics.sh
   ```

3. Run the performance test (10 concurrent requests):
   ```bash
   ./perf-test.sh
   ```

## Configuration

- **`toxiproxy.json`** — Defines the proxy `service-a` listening on port `9753`, forwarding to the upstream service on port `8080`.
- **`setup-toxics.sh`** — Adds a 5000ms downstream latency toxic via the Toxiproxy API (`localhost:8474`).
- **`perf-test.sh`** — Sends 10 concurrent requests to `localhost:9753` and reports per-request timing and overall wall time.

## Managing Toxics

The Toxiproxy API is available at `http://localhost:8474`. Examples:

```bash
# List all proxies
curl http://localhost:8474/proxies | jq .

# List toxics on service-a
curl http://localhost:8474/proxies/service-a/toxics | jq .

# Remove the latency toxic
curl -X DELETE http://localhost:8474/proxies/service-a/toxics/latency_downstream
```
