package com.demo.orders;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/orders")
class OrderController {

	private final OrderService orderService;

	OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	ResponseEntity<Map<String, Object>> createOrder(@RequestBody OrderRequest request) {
		Map<String, Object> result = orderService.placeOrder(request);
		return ResponseEntity.created(URI.create("/orders/" + result.get("orderId"))).body(result);
	}

}
