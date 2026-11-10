package com.meliskara.crypto_trading_simulator.order;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MatchTest {

	private final UUID makerOrderId = UUID.randomUUID();
	private final UUID makerUserId = UUID.randomUUID();
	private final UUID takerOrderId = UUID.randomUUID();
	private final UUID takerUserId = UUID.randomUUID();

	@Test
	void takerIsBuyerWhenTakerSideIsBuy() {
		Match match = match(OrderSide.BUY);

		assertThat(match.buyOrderId()).isEqualTo(takerOrderId);
		assertThat(match.buyerId()).isEqualTo(takerUserId);
		assertThat(match.sellOrderId()).isEqualTo(makerOrderId);
		assertThat(match.sellerId()).isEqualTo(makerUserId);
	}

	@Test
	void makerIsBuyerWhenTakerSideIsSell() {
		Match match = match(OrderSide.SELL);

		assertThat(match.buyOrderId()).isEqualTo(makerOrderId);
		assertThat(match.buyerId()).isEqualTo(makerUserId);
		assertThat(match.sellOrderId()).isEqualTo(takerOrderId);
		assertThat(match.sellerId()).isEqualTo(takerUserId);
	}

	@Test
	void quoteAmountIsPriceTimesQuantity() {
		Match match = match(OrderSide.BUY);

		assertThat(match.quoteAmount()).isEqualByComparingTo("6000");
	}

	private Match match(OrderSide takerSide) {
		return new Match(makerOrderId, makerUserId, takerOrderId, takerUserId,
				takerSide, new BigDecimal("60000"), new BigDecimal("0.1"));
	}
}