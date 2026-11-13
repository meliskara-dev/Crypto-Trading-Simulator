package com.meliskara.crypto_trading_simulator.order;

import com.meliskara.crypto_trading_simulator.common.outbox.OutboxService;
import com.meliskara.crypto_trading_simulator.market.Symbol;
import com.meliskara.crypto_trading_simulator.wallet.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

	private static final Instant NOW = Instant.parse("2026-11-13T10:00:00Z");
	private static final UUID BUYER = UUID.randomUUID();
	private static final UUID SELLER = UUID.randomUUID();
	private static final UUID BUY_ORDER_ID = UUID.randomUUID();
	private static final UUID SELL_ORDER_ID = UUID.randomUUID();

	@Mock
	private OrderRepository orderRepository;
	@Mock
	private TradeRepository tradeRepository;
	@Mock
	private WalletService walletService;
	@Mock
	private OutboxService outboxService;

	private SettlementService settlementService;

	@BeforeEach
	void setUp() {
		settlementService = new SettlementService(orderRepository, tradeRepository, walletService, outboxService);
	}

	@Test
	void fullFillAtMakerPriceMovesFundsAndRefundsPriceImprovement() {
		Order buy = order(BUYER, OrderSide.BUY, "61000", "0.1");
		Order sell = order(SELLER, OrderSide.SELL, "60000", "0.1");
		stubOrders(buy, sell);
		when(tradeRepository.save(any(Trade.class))).thenAnswer(inv -> inv.getArgument(0));

		// maker = SELL @ 60000, taker = BUY @ 61000
		Match match = new Match(SELL_ORDER_ID, SELLER, BUY_ORDER_ID, BUYER, OrderSide.BUY,
				new BigDecimal("60000"), new BigDecimal("0.1"));

		settlementService.settle(Symbol.BTCUSDT, List.of(match), NOW);

		assertThat(buy.getStatus()).isEqualTo(OrderStatus.FILLED);
		assertThat(sell.getStatus()).isEqualTo(OrderStatus.FILLED);

		verify(walletService).transferLocked(eq(BUYER), eq(SELLER), eq("USDT"), amount("6000"), eq(NOW));
		verify(walletService).transferLocked(eq(SELLER), eq(BUYER), eq("BTC"), amount("0.1"), eq(NOW));
		// 61000 kilitlendi, 60000 harcandı: 0.1 × 1000 = 100 USDT geri açılır
		verify(walletService).unlock(eq(BUYER), eq("USDT"), amount("100"), eq(NOW));

		verify(tradeRepository).save(any(Trade.class));
		verify(outboxService).append(eq(TradeEventTopicConfig.TRADE_EVENTS_TOPIC), eq("BTCUSDT"),
				any(TradeEvent.class), eq(NOW));
	}

	@Test
	void partialFillLeavesBiggerOrderPartiallyFilledWithoutRefund() {
		Order buy = order(BUYER, OrderSide.BUY, "60000", "0.1");
		Order sell = order(SELLER, OrderSide.SELL, "60000", "0.3");
		stubOrders(buy, sell);
		when(tradeRepository.save(any(Trade.class))).thenAnswer(inv -> inv.getArgument(0));

		Match match = new Match(SELL_ORDER_ID, SELLER, BUY_ORDER_ID, BUYER, OrderSide.BUY,
				new BigDecimal("60000"), new BigDecimal("0.1"));

		settlementService.settle(Symbol.BTCUSDT, List.of(match), NOW);

		assertThat(buy.getStatus()).isEqualTo(OrderStatus.FILLED);
		assertThat(sell.getStatus()).isEqualTo(OrderStatus.PARTIALLY_FILLED);
		assertThat(sell.remainingQuantity()).isEqualByComparingTo("0.2");
		verify(walletService, never()).unlock(any(), anyString(), any(), any());
	}

	@Test
	void missingOrderFailsBeforeAnyMoneyMoves() {
		when(orderRepository.findById(BUY_ORDER_ID)).thenReturn(Optional.empty());

		Match match = new Match(SELL_ORDER_ID, SELLER, BUY_ORDER_ID, BUYER, OrderSide.BUY,
				new BigDecimal("60000"), new BigDecimal("0.1"));

		assertThatThrownBy(() -> settlementService.settle(Symbol.BTCUSDT, List.of(match), NOW))
				.isInstanceOf(IllegalStateException.class);

		verifyNoInteractions(walletService, tradeRepository, outboxService);
	}

	private void stubOrders(Order buy, Order sell) {
		when(orderRepository.findById(BUY_ORDER_ID)).thenReturn(Optional.of(buy));
		when(orderRepository.findById(SELL_ORDER_ID)).thenReturn(Optional.of(sell));
	}

	private static Order order(UUID userId, OrderSide side, String price, String quantity) {
		return new Order(userId, Symbol.BTCUSDT, side, OrderType.LIMIT,
				new BigDecimal(price), new BigDecimal(quantity), UUID.randomUUID().toString(), NOW);
	}

	private static BigDecimal amount(String expected) {
		return argThat(actual -> actual.compareTo(new BigDecimal(expected)) == 0);
	}
}