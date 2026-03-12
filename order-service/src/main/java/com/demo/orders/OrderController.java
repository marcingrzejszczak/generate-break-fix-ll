package com.demo.orders;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletionStage;

@RestController
class OrderController {

	private final OrderService orderService;

	OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	// TASK.md: POST /orders
	@PostMapping("/orders")
	CompletionStage<ResponseEntity<Map<String, Object>>> createOrder(@RequestBody OrderRequest request) {
		return orderService.createOrder(request).thenApply(result -> {
			if ("FAILED".equals(result.get("orderStatus"))) {
				return ResponseEntity.status(503).body(result);
			}
			return ResponseEntity.created(URI.create("/orders/" + result.get("orderId"))).body(result);
		});
	}

	// TASK2.md: POST /place-order
	@PostMapping("/place-order")
	CompletionStage<ResponseEntity<Map<String, Object>>> placeOrder(@RequestBody OrderRequest request) {
		return orderService.placeOrder(request).thenApply(result -> {
			if ("FAILED".equals(result.get("status"))) {
				return ResponseEntity.status(503).body(result);
			}
			return ResponseEntity.status(201).body(result);
		});
	}

}
