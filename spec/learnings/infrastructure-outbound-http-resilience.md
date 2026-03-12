# Outbound HTTP Resilience

**Category:** Infrastructure
**Last Updated:** 2026-03-10

## Related Files

- `order-service/src/main/java/com/demo/orders/OrderService.java`
- `order-service/src/main/resources/application.properties`
- `order-service/pom.xml`

## Findings

### 2026-03-10: Outbound HTTP calls without circuit breakers cause cascading failures

**Discovery:** Under load testing (100 req/s via k6), introducing 5s latency on the
downstream Payment Service caused the Order Service to exhaust its Tomcat thread pool
(200 threads). All threads were blocked waiting on the slow downstream call, making the
entire Order Service unresponsive — even for requests unrelated to payments.

**Evidence:**
- k6 load test with 5s toxic latency: 66% of requests timed out (1600 out of 2400)
- Average response time climbed to 13s, p95 hit 15s
- Thread pool saturation occurred within ~2 seconds (200 threads / 100 req/s)

**Root Cause:** The `RestClient.post()` call had no timeout or circuit breaker. Each
request held a Tomcat thread for the full 5s downstream latency, quickly exhausting the
pool.

**Resolution:** Wrap every outbound HTTP call in a circuit breaker with a timeout.

### Rule: All outbound HTTP calls MUST be wrapped in a circuit breaker

Every HTTP call to an external service must have:

1. **Timeout** — fail fast if the downstream doesn't respond within a threshold (e.g. 1s)
2. **Circuit breaker** — stop calling a failing service and fail fast for a cooldown period
3. **Fallback** — return a degraded response instead of propagating the failure upstream

This applies to:
- REST API calls to other microservices
- Calls to third-party APIs
- Any outbound HTTP request that crosses a network boundary

Without these protections, a single slow downstream service can take down the entire
upstream service through thread pool exhaustion.

## Change Log

| Date | Change |
|------|--------|
| 2026-03-10 | Initial discovery during load testing with Toxiproxy latency injection |
