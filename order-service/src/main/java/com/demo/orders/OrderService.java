package com.demo.orders;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Service
class OrderService {

	private final RestClient restClient;

	OrderService(@Value("${payment.service.url}") String paymentServiceUrl) {
		this.restClient = RestClient.builder().baseUrl(paymentServiceUrl).build();
	}

	Map<String, Object> placeOrder(OrderRequest request) {
		String orderId = UUID.randomUUID().toString();

		Map<String, Object> paymentRequest = Map.of(
				"orderId", orderId,
				"amount", request.amount());

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
	}

}
