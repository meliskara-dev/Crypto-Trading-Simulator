package com.meliskara.crypto_trading_simulator.order;

import com.meliskara.crypto_trading_simulator.market.Symbol;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
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

	@Test
	void noMatchWhenPricesDoNotCross() {
		book.add(order(OrderSide.SELL, "60100"));

		List<Match> matches = book.match(order(OrderSide.BUY, "60000"));

		assertThat(matches).isEmpty();

		assertThat(book.bestBid().orElseThrow()).isEqualByComparingTo("60000");

		assertThat(book.bestAsk().orElseThrow()).isEqualByComparingTo("60100");
	}


	@Test
	void fullMatchUsesMakerPrice() {
		RestingOrder maker = order(OrderSide.SELL, "60000");
		book.add(maker);
		RestingOrder taker = order(OrderSide.BUY, "61000");

		List<Match> matches = book.match(taker);

		assertThat(matches).hasSize(1);
		Match match = matches.getFirst();
		assertThat(match.makerOrderId()).isEqualTo(maker.getOrderId());
		assertThat(match.takerOrderId()).isEqualTo(taker.getOrderId());
		assertThat(match.takerSide()).isEqualTo(OrderSide.BUY);
		assertThat(match.price()).isEqualByComparingTo("60000");
		assertThat(match.quantity()).isEqualByComparingTo("1");
		assertThat(book.bestBid()).isEmpty();
		assertThat(book.bestAsk()).isEmpty();
	}

	@Test
	void partialFillRestsRemainderInBook() {
		book.add(order(OrderSide.SELL, "60000", "0.3"));
		RestingOrder taker = order(OrderSide.BUY, "60000", "1");

		List<Match> matches = book.match(taker);

		assertThat(matches).hasSize(1);
		assertThat(matches.getFirst().quantity()).isEqualByComparingTo("0.3");
		assertThat(taker.getRemaining()).isEqualByComparingTo("0.7");
		assertThat(book.bestAsk()).isEmpty();
		assertThat(book.bestBid().orElseThrow()).isEqualByComparingTo("60000");
	}

	@Test
	void bestPriceMatchesFirstAcrossLevels() {
		book.add(order(OrderSide.SELL, "60100", "0.5"));
		book.add(order(OrderSide.SELL, "60000", "0.5"));

		List<Match> matches = book.match(order(OrderSide.BUY, "60100", "1"));

		assertThat(matches).hasSize(2);
		assertThat(matches.get(0).price()).isEqualByComparingTo("60000");
		assertThat(matches.get(1).price()).isEqualByComparingTo("60100");
		assertThat(book.bestAsk()).isEmpty();
		assertThat(book.bestBid()).isEmpty();
	}

	@Test
	void earlierOrderMatchesFirstAtSamePrice() {
		RestingOrder first = order(OrderSide.SELL, "60000");
		RestingOrder second = order(OrderSide.SELL, "60000.00");
		book.add(first);
		book.add(second);

		List<Match> matches = book.match(order(OrderSide.BUY, "60000"));

		assertThat(matches).hasSize(1);
		assertThat(matches.getFirst().makerOrderId()).isEqualTo(first.getOrderId());
		assertThat(book.bestAsk().orElseThrow()).isEqualByComparingTo("60000");
	}

	private static RestingOrder order(OrderSide side, String price) {
		return order(side, price, "1");
	}

	private static RestingOrder order(OrderSide side, String price, String quantity) {
		return new RestingOrder(UUID.randomUUID(), UUID.randomUUID(), side, new BigDecimal(price), new BigDecimal(quantity));
	}
}
