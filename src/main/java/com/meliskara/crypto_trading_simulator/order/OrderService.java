package com.meliskara.crypto_trading_simulator.order;

import com.meliskara.crypto_trading_simulator.wallet.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
class OrderService {

	private static final String QUOTE_ASSET = "USDT";

	private final OrderRepository orderRepository;
	private final WalletService walletService;

	OrderService(OrderRepository orderRepository, WalletService walletService) {
		this.orderRepository = orderRepository;
		this.walletService = walletService;
	}

	@Transactional
	Order placeOrder(UUID userId, String idempotencyKey, CreateOrderRequest request) {
		var existing = orderRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
		if (existing.isPresent()) {
			return existing.get();
		}

		if (request.type() != OrderType.LIMIT) {
			throw new UnsupportedOrderTypeException(request.type());
		}

		Instant now = Instant.now();
		lockFunds(userId, request, now);

		Order order = new Order(
				userId,
				request.symbol(),
				request.side(),
				request.type(),
				request.price(),
				request.quantity(),
				idempotencyKey,
				now
		);
		return orderRepository.save(order);
	}

	@Transactional(readOnly = true)
	Optional<Order> findByIdempotencyKey(UUID userId, String idempotencyKey) {
		return orderRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
	}

	private void lockFunds(UUID userId, CreateOrderRequest request, Instant now) {
		if (request.side() == OrderSide.BUY) {
			BigDecimal cost = request.price().multiply(request.quantity());
			walletService.lock(userId, QUOTE_ASSET, cost, now);
		} else {
			walletService.lock(userId, request.symbol().baseAsset(), request.quantity(), now);
		}
	}
}
