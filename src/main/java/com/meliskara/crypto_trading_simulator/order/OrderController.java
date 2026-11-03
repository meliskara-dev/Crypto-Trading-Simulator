package com.meliskara.crypto_trading_simulator.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
class OrderController {

	private final OrderService orderService;

	OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	OrderResponse placeOrder(
			@AuthenticationPrincipal Jwt jwt,
			@RequestHeader("Idempotency-Key") @NotBlank @Size(max = 64) String idempotencyKey,
			@Valid @RequestBody CreateOrderRequest request) {
		UUID userId = UUID.fromString(jwt.getSubject());
		try {
			return OrderResponse.from(orderService.placeOrder(userId, idempotencyKey, request));
		} catch (DataIntegrityViolationException ex) {
			return orderService.findByIdempotencyKey(userId, idempotencyKey)
					.map(OrderResponse::from)
					.orElseThrow(() -> ex);
		}
	}
}
