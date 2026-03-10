# Lightning Session

Two services that communicate with each other through a Toxiproxy-managed network, allowing simulation of adverse network conditions like latency.

## Architecture

```
Client -> Order Service (:3456) -> Proxy (:9753) -> Payment Service (:8080)
```

Toxiproxy sits between the Order Service and the Payment Service, allowing simulation of adverse network conditions like latency.

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

3. Run the performance test (100 requests, 10 concurrent):
   ```bash
   ./perf-test.sh
   ```

## Performance Testing

`perf-test.sh` uses [hey](https://github.com/rakyll/hey) via Docker to load-test the Order Service. No local install required.

```bash
# Default: 100 requests, 10 concurrent
./perf-test.sh
```

It sends `POST /orders` requests with a JSON payload to the Order Service on port 3456. The output includes latency distribution, throughput, and status code breakdown.

## Configuration

- **`toxiproxy.json`** — Defines the proxy listening on port `9753`, forwarding to the Payment Service on port `8080`.
- **`setup-toxics.sh`** — Adds a 5000ms downstream latency toxic via the Toxiproxy API (`localhost:8474`).
- **`perf-test.sh`** — Runs `hey` in Docker to send concurrent POST requests to the Order Service and report performance metrics.

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
