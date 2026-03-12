# TASK 2: Resilient Order Placement System

## Overview

Extends the TASK 1 order placement system with a circuit breaker and timeout on the
outbound payment call, preventing cascading failures when the Payment Service is slow
or unavailable.

## What Changed from TASK 1

The Order Service now wraps the call to the Payment Service in:

1. **A timeout (1 second)** — if the payment call takes longer than 1s, it is cancelled
2. **A circuit breaker** — if enough calls fail, the circuit opens and subsequent calls
   fail fast without reaching the Payment Service
3. **A fallback** — when the timeout or circuit breaker triggers, the order is returned
   with a `FAILED` status and an error message instead of a 500 error

The Payment Service and proxy are unchanged.

## Behavior

### Place an Order — Happy Path

Identical to TASK 1. When the Payment Service responds within 1 second:

**Trigger:** `POST /orders`

**Response (HTTP 201):**
```json
{
  "orderId": "<uuid>",
  "productId": "<string>",
  "amount": <number>,
  "orderStatus": "CONFIRMED",
  "paymentId": "<string>"
}
```

### Place an Order — Payment Timeout

When the Payment Service takes longer than 1 second to respond:

**Response (HTTP 503):**
```json
{
  "orderId": "<uuid>",
  "productId": "<string>",
  "amount": <number>,
  "orderStatus": "FAILED",
  "error": "Payment service unavailable: <timeout message>"
}
```

The request is cancelled after 1 second. The Tomcat thread is released promptly.

### Place an Order — Circuit Open

When the circuit breaker is open (too many recent failures):

**Response (HTTP 503):**
```json
{
  "orderId": "<uuid>",
  "productId": "<string>",
  "amount": <number>,
  "orderStatus": "FAILED",
  "error": "Payment service unavailable: <circuit breaker open message>"
}
```

The Payment Service is NOT called. The response is returned in milliseconds.

## Circuit Breaker Configuration

| Parameter | Value | Meaning |
|-----------|-------|---------|
| Sliding window size | 10 | Evaluates the last 10 calls |
| Failure rate threshold | 50% | Opens the circuit when 5 out of 10 calls fail |
| Wait duration in open state | 10 seconds | How long the circuit stays open before allowing probe requests |
| Permitted calls in half-open state | 3 | Number of probe requests to test if the downstream has recovered |

### Circuit Breaker State Machine

```
CLOSED  (all calls go through)
   │
   │  failure rate >= 50% over last 10 calls
   ▼
OPEN    (all calls fail fast via fallback, payment service not called)
   │
   │  after 10 seconds
   ▼
HALF-OPEN (3 probe calls allowed through)
   │
   ├── probes succeed → back to CLOSED
   └── probes fail    → back to OPEN
```

## Timeout Configuration

| Parameter | Value |
|-----------|-------|
| Timeout duration | 1 second |

If the Payment Service does not respond within 1 second, the call is cancelled and
the fallback is invoked.

## Constraints

- All constraints from TASK 1 still apply
- Every outbound HTTP call MUST be wrapped in a circuit breaker with a timeout
- The fallback MUST return a valid response (not an exception) so the client gets
  a structured error instead of a 500
- The Order Service MUST remain responsive even when the Payment Service is degraded

## Expected Behavior Under Load

With 100 req/s and 5s downstream latency:

- **TASK 1:** Thread pool exhaustion in ~2s, 66%+ request failures, 13s+ avg latency
- **TASK 2:** Requests timeout at 1s, circuit opens after ~5 failures, subsequent
  requests fail fast in milliseconds. The Order Service remains responsive throughout.
