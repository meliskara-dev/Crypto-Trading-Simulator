package com.meliskara.crypto_trading_simulator.order;

import com.meliskara.crypto_trading_simulator.market.Symbol;
import com.meliskara.crypto_trading_simulator.wallet.InsufficientBalanceException;
import com.meliskara.crypto_trading_simulator.wallet.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	private static final UUID USER_ID = UUID.randomUUID();
	private static final String KEY = "key-1";

	@Mock
	private OrderRepository orderRepository;
	@Mock
	private WalletService walletService;
	@Mock
	private ApplicationEventPublisher eventPublisher;

	private OrderService orderService;

	@BeforeEach
	void setUp() {
		orderService = new OrderService(orderRepository, walletService, eventPublisher);
	}

	@Test
	void buyOrderLocksPriceTimesQuantityInUsdt() {
		when(orderRepository.findByUserIdAndIdempotencyKey(USER_ID, KEY)).thenReturn(Optional.empty());
		when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

		Order order = orderService.placeOrder(USER_ID, KEY, limit(OrderSide.BUY, "60000", "0.1"));

		assertThat(order.getStatus()).isEqualTo(OrderStatus.NEW);
		verify(walletService).lock(eq(USER_ID), eq("USDT"),
				argThat(amount -> amount.compareTo(new BigDecimal("6000")) == 0), any(Instant.class));
		verify(eventPublisher).publishEvent(any(OrderPlacedEvent.class));
	}

	@Test
	void sellOrderLocksQuantityInBaseAsset() {
		when(orderRepository.findByUserIdAndIdempotencyKey(USER_ID, KEY)).thenReturn(Optional.empty());
		when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

		orderService.placeOrder(USER_ID, KEY, limit(OrderSide.SELL, "60000", "0.5"));

		verify(walletService).lock(eq(USER_ID), eq("BTC"),
				argThat(amount -> amount.compareTo(new BigDecimal("0.5")) == 0), any(Instant.class));
	}

	@Test
	void sameIdempotencyKeyReturnsExistingOrderWithoutLockingAgain() {
		Order existing = new Order(USER_ID, Symbol.BTCUSDT, OrderSide.BUY, OrderType.LIMIT,
				new BigDecimal("60000"), new BigDecimal("0.1"), KEY, Instant.now());
		when(orderRepository.findByUserIdAndIdempotencyKey(USER_ID, KEY)).thenReturn(Optional.of(existing));

		Order result = orderService.placeOrder(USER_ID, KEY, limit(OrderSide.BUY, "60000", "0.1"));

		assertThat(result).isSameAs(existing);
		verifyNoInteractions(walletService, eventPublisher);
		verify(orderRepository, never()).save(any());
	}

	@Test
	void insufficientBalanceSavesNothingAndPublishesNothing() {
		when(orderRepository.findByUserIdAndIdempotencyKey(USER_ID, KEY)).thenReturn(Optional.empty());
		doThrow(new InsufficientBalanceException("USDT"))
				.when(walletService).lock(any(), anyString(), any(), any());

		assertThatThrownBy(() -> orderService.placeOrder(USER_ID, KEY, limit(OrderSide.BUY, "60000", "1")))
				.isInstanceOf(InsufficientBalanceException.class);

		verify(orderRepository, never()).save(any());
		verifyNoInteractions(eventPublisher);
	}

	@Test
	void marketOrderIsRejectedBeforeAnyLock() {
		when(orderRepository.findByUserIdAndIdempotencyKey(USER_ID, KEY)).thenReturn(Optional.empty());
		var request = new CreateOrderRequest(Symbol.BTCUSDT, OrderSide.BUY, OrderType.MARKET,
				null, new BigDecimal("0.1"));

		assertThatThrownBy(() -> orderService.placeOrder(USER_ID, KEY, request))
				.isInstanceOf(UnsupportedOrderTypeException.class);

		verifyNoInteractions(walletService, eventPublisher);
	}

	private static CreateOrderRequest limit(OrderSide side, String price, String quantity) {
		return new CreateOrderRequest(Symbol.BTCUSDT, side, OrderType.LIMIT,
				new BigDecimal(price), new BigDecimal(quantity));
	}
}