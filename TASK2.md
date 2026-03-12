# TASK 2 --- Payment Authorization Integration

This task is to be done IN ADDITION to the one in TASK.md. Don't modify the existing feature, add a new one. It requires new endpoints in both order service and payment service. Do not reuse existing ones from TASK.md.

## Overview

The Order Service must now integrate with a Payment Service in order to
authorize payments before confirming an order.

The Payment Service runs as a separate application.

The system consists of:

-   **Order Service (Java)** running on **port 3456**
-   **Payment Service (TypeScript)** running on **port 8080**
-   **Proxy** running on **port 9753**

The Order Service must call the Payment Service **through the proxy**.

------------------------------------------------------------------------

## User Story

**As the Order Service**

I want to request payment authorization from the Payment Service

So that I can confirm whether an order can be completed.

------------------------------------------------------------------------

## High-Level Flow

1.  A client sends a request to the Order Service.
2.  The Order Service prepares a payment authorization request.
3.  The Order Service calls the Payment Service through the proxy.
4.  The Payment Service returns the payment result.
5.  The Order Service responds to the client.

------------------------------------------------------------------------

## Example Interaction

### Client request (Order Service)

Client calls the Order Service:

POST http://localhost:3456/place-order

Example payload:

``` json
{
  "productId": "book-123",
  "amount": 49.99
}
```

------------------------------------------------------------------------

### Payment authorization request

The Order Service calls the Payment Service through the proxy:

POST http://localhost:9753/authorize-payment

Example payload:

``` json
{
  "orderId": "order-abc-123",
  "amount": 49.99
}
```

------------------------------------------------------------------------

### Payment Service response

The Payment Service returns the authorization result.

Example response:

``` json
{
  "status": "AUTHORIZED"
}
```

Possible values:

-   AUTHORIZED
-   DECLINED

------------------------------------------------------------------------

## Order Service response

The Order Service returns the result to the client.

Example response:

``` json
{
  "orderId": "order-abc-123",
  "status": "CONFIRMED"
}
```

------------------------------------------------------------------------

## Operational Notes

-   The Order Service runs on **port 3456**
-   The Payment Service runs on **port 8080**
-   The Order Service **must call the Payment Service via the proxy on
    port 9753**
-   External clients interact only with the Order Service