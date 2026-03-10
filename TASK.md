# Demo Business Context

## Overview

We are building a small online ordering system composed of two
services.\
The system allows customers to place an order and process a payment
before confirming the purchase.

The architecture consists of:

-   **Order Service** (Java) running on **port 3456**
-   **Payment Service** (TypeScript) running on **port 8080**
-   A **network proxy** running on **port 9753** (running through docker-compose)

The Order Service communicates with the Payment Service through the
proxy.

------------------------------------------------------------------------

## User Story

**As a customer**

I want to place an order and have the system process my payment

So that my purchase can be confirmed.

------------------------------------------------------------------------

## High-Level Flow

1.  A client sends a request to the **Order Service**.
2.  The Order Service creates an order.
3.  The Order Service calls the **Payment Service** to authorize the
    payment.
4.  If the payment succeeds, the order is confirmed.
5.  If the payment fails or does not respond in time, the order cannot
    be completed.

------------------------------------------------------------------------

## System Architecture

    Client
       |
       v
    Order Service (Java)
    localhost:3456
       |
       v
    Proxy
    localhost:9753
       |
       v
    Payment Service (TypeScript)
    localhost:8080

The proxy sits between the services and forwards traffic from the Order
Service to the Payment Service.

All calls from the Order Service to the Payment Service must go through
the proxy.

------------------------------------------------------------------------

## Example Interaction

### Client places an order

    POST http://localhost:3456/orders

Example payload:

``` json
{
  "productId": "book-123",
  "amount": 49.99
}
```

## Operational Notes

-   The Order Service **must call the Payment Service via the proxy on
    port 9753**.
-   The Payment Service itself runs on **port 8080**.
