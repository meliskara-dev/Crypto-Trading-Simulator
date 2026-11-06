package com.meliskara.crypto_trading_simulator.order;

import com.meliskara.crypto_trading_simulator.market.Symbol;

import java.math.BigDecimal;
import java.util.*;

final class OrderBook {

	private final Symbol symbol;
	private final NavigableMap<BigDecimal, Deque<RestingOrder>> bids =
			new TreeMap<>(Comparator.reverseOrder());
	private final NavigableMap<BigDecimal, Deque<RestingOrder>> asks =
			new TreeMap<>();

	OrderBook(Symbol symbol) {
		this.symbol = symbol;
	}

	void add(RestingOrder order) {
		sideOf(order.getSide())
				.computeIfAbsent(order.getPrice(), price -> new ArrayDeque<>())
				.addLast(order);
	}

	Optional<BigDecimal> bestBid() {
		return bids.isEmpty() ? Optional.empty() :
				Optional.of(bids.firstKey());
	}

	Optional<BigDecimal> bestAsk() {
		return asks.isEmpty() ? Optional.empty() :
				Optional.of(asks.firstKey());
	}

	Symbol getSymbol() {
		return symbol;
	}

	private NavigableMap<BigDecimal, Deque<RestingOrder>> sideOf(OrderSide side) {
		return side == OrderSide.BUY ? bids : asks;
	}
}
