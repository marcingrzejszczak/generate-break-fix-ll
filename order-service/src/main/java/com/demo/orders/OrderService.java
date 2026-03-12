package com.demo.orders;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Service
class OrderService {

	private final RestClient restClient;

	OrderService(@Value("${payment.service.url}") String paymentServiceUrl) {
		this.restClient = RestClient.builder().baseUrl(paymentServiceUrl).build();
	}

	// TASK.md: calls POST /payments, returns full order details
	@CircuitBreaker(name = "payment", fallbackMethod = "createOrderFallback")
	@TimeLimiter(name = "payment", fallbackMethod = "createOrderFallback")
	CompletionStage<Map<String, Object>> createOrder(OrderRequest request) {
		return CompletableFuture.supplyAsync(() -> {
			String orderId = UUID.randomUUID().toString();

			Map<String, Object> paymentRequest = Map.of(
					"orderId", orderId,
					"amount", request.amount());

			@SuppressWarnings("unchecked")
			Map<String, Object> paymentResponse = restClient.post()
					.uri("/payments")
					.header("Content-Type", "application/json")
					.body(paymentRequest)
					.retrieve()
					.body(Map.class);

			String paymentStatus = (String) paymentResponse.get("status");
			String orderStatus = "AUTHORIZED".equals(paymentStatus) ? "CONFIRMED" : "FAILED";

			return Map.of(
					"orderId", orderId,
					"productId", request.productId(),
					"amount", request.amount(),
					"orderStatus", orderStatus,
					"paymentId", paymentResponse.get("paymentId"));
		});
	}

	CompletionStage<Map<String, Object>> createOrderFallback(OrderRequest request, Throwable t) {
		String orderId = UUID.randomUUID().toString();
		return CompletableFuture.completedFuture(Map.of(
				"orderId", orderId,
				"productId", request.productId(),
				"amount", request.amount(),
				"orderStatus", "FAILED",
				"error", "Payment service unavailable: " + t.getMessage()));
	}

	// TASK2.md: calls POST /authorize-payment, returns orderId + status
	@CircuitBreaker(name = "payment", fallbackMethod = "placeOrderFallback")
	@TimeLimiter(name = "payment", fallbackMethod = "placeOrderFallback")
	CompletionStage<Map<String, Object>> placeOrder(OrderRequest request) {
		return CompletableFuture.supplyAsync(() -> {
			String orderId = UUID.randomUUID().toString();

			Map<String, Object> paymentRequest = Map.of(
					"orderId", orderId,
					"amount", request.amount());

			@SuppressWarnings("unchecked")
			Map<String, Object> paymentResponse = restClient.post()
					.uri("/authorize-payment")
					.header("Content-Type", "application/json")
					.body(paymentRequest)
					.retrieve()
					.body(Map.class);

			String paymentStatus = (String) paymentResponse.get("status");
			String status = "AUTHORIZED".equals(paymentStatus) ? "CONFIRMED" : "FAILED";

			return Map.of(
					"orderId", orderId,
					"status", status);
		});
	}

	CompletionStage<Map<String, Object>> placeOrderFallback(OrderRequest request, Throwable t) {
		String orderId = UUID.randomUUID().toString();
		return CompletableFuture.completedFuture(Map.of(
				"orderId", orderId,
				"status", "FAILED",
				"error", "Payment service unavailable: " + t.getMessage()));
	}

}
