package com.meliskara.crypto_trading_simulator.order;

import com.meliskara.crypto_trading_simulator.market.Symbol;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderBookTest {

	private final OrderBook book = new OrderBook(Symbol.BTCUSDT);

	@Test
	void emptyBookHasNoBestPrices() {
		assertThat(book.bestBid()).isEmpty();
		assertThat(book.bestAsk()).isEmpty();
	}

	@Test
	void bestBidIsHighestBuyPrice() {
		book.add(order(OrderSide.BUY, "60000"));
		book.add(order(OrderSide.BUY, "60100"));
		book.add(order(OrderSide.BUY, "59900"));

		assertThat(book.bestBid().orElseThrow()).isEqualByComparingTo("60100");
	}

	@Test
	void bestAskIsLowestSellPrice() {
		book.add(order(OrderSide.SELL, "60300"));
		book.add(order(OrderSide.SELL, "60200"));
		book.add(order(OrderSide.SELL, "60400"));

		assertThat(book.bestAsk().orElseThrow()).isEqualByComparingTo("60200");
	}

	@Test
	void buyOrdersDoNotAffectAskSide() {
		book.add(order(OrderSide.BUY, "60000"));

		assertThat(book.bestAsk()).isEmpty();
	}

	private static RestingOrder order(OrderSide side, String price) {
		return new RestingOrder(UUID.randomUUID(), UUID.randomUUID(), side, new BigDecimal(price), new BigDecimal("1"));
	}
}
