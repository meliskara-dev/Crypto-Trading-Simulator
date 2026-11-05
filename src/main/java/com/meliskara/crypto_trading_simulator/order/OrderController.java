package com.meliskara.crypto_trading_simulator.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

	@GetMapping("/{id}")
	OrderResponse getOrder(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		UUID userId = UUID.fromString(jwt.getSubject());
		return OrderResponse.from(orderService.getOrder(userId, id));
	}

	@GetMapping
	List<OrderResponse> getOrders(
			@AuthenticationPrincipal Jwt jwt,
			@RequestParam(defaultValue = "50") @Min(1) @Max(200) int limit) {
		UUID userId = UUID.fromString(jwt.getSubject());
		return orderService.getOrders(userId, limit).stream()
				.map(OrderResponse::from)
				.toList();
	}

	@DeleteMapping("/{id}")
	OrderResponse cancelOrder(@AuthenticationPrincipal Jwt jwt,
							  @PathVariable UUID id) {
		UUID userId = UUID.fromString(jwt.getSubject());
		return OrderResponse.from(orderService.cancelOrder(userId, id));
	}

}
