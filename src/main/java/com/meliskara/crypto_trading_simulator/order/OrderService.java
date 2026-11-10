package com.meliskara.crypto_trading_simulator.order;

import com.meliskara.crypto_trading_simulator.market.Symbol;
import com.meliskara.crypto_trading_simulator.wallet.WalletService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
class OrderService {

	static final String QUOTE_ASSET = "USDT";

	private final OrderRepository orderRepository;
	private final WalletService walletService;

	private final ApplicationEventPublisher eventPublisher;

	OrderService(OrderRepository orderRepository, WalletService walletService,
				 ApplicationEventPublisher eventPublisher) {
		this.orderRepository = orderRepository;
		this.walletService = walletService;
		this.eventPublisher = eventPublisher;
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
		Order saved = orderRepository.save(order);

		eventPublisher.publishEvent(OrderPlacedEvent.from(saved));
		return saved;
	}

	@Transactional(readOnly = true)
	Optional<Order> findByIdempotencyKey(UUID userId, String idempotencyKey) {
		return orderRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
	}

	@Transactional(readOnly = true)
	Order getOrder(UUID userId, UUID orderId) {
		return orderRepository.findByIdAndUserId(orderId, userId)
				.orElseThrow(() -> new OrderNotFoundException(orderId));
	}

	@Transactional(readOnly = true)
	List<Order> getOrders(UUID userId, int limit) {
		return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, Limit.of(limit));
	}

	@Transactional(readOnly = true)
	List<RestingOrder> findOpenOrders(Symbol symbol) {
		List<OrderStatus> openStatuses =
				Arrays.stream(OrderStatus.values())
						.filter(OrderStatus::isOpen)
						.toList();
		return orderRepository.findBySymbolAndStatusInOrderByCreatedAtAsc(symbol, openStatuses).stream()
				.map(Order::toRestingOrder)
				.toList();
	}

	@Transactional
	Order cancelOrder(UUID userId, UUID orderId) {
		Order order = orderRepository.findByIdAndUserId(orderId, userId)
				.orElseThrow(() -> new OrderNotFoundException(orderId));

		Instant now = Instant.now();
		order.cancel(now);
		releaseFunds(order, now);
		eventPublisher.publishEvent(new OrderCancelledEvent(order.getId(), order.getSymbol()));
		return order;
	}

	private void lockFunds(UUID userId, CreateOrderRequest request, Instant now) {
		if (request.side() == OrderSide.BUY) {
			BigDecimal cost = request.price().multiply(request.quantity());
			walletService.lock(userId, QUOTE_ASSET, cost, now);
		} else {
			walletService.lock(userId, request.symbol().baseAsset(), request.quantity(), now);
		}
	}

	private void releaseFunds(Order order, Instant now) {
		BigDecimal remaining = order.remainingQuantity();
		if (order.getSide() == OrderSide.BUY) {
			BigDecimal amount = order.getPrice().multiply(remaining);
			walletService.unlock(order.getUserId(), QUOTE_ASSET, amount, now);
		} else {
			walletService.unlock(order.getUserId(),
					order.getSymbol().baseAsset(), remaining, now);
		}
	}
}
