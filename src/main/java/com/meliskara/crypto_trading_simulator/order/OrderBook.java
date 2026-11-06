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

	List<Match> match(RestingOrder incoming) {
		NavigableMap<BigDecimal, Deque<RestingOrder>> opposite =
				oppositeOf(incoming.getSide());
		List<Match> matches = new ArrayList<>();

		while (!incoming.isFilled() && !opposite.isEmpty()) {
			Map.Entry<BigDecimal, Deque<RestingOrder>> bestLevel =
					opposite.firstEntry();
			if (!crosses(incoming, bestLevel.getKey())) {
				break;
			}

			Deque<RestingOrder> queue = bestLevel.getValue();
			RestingOrder maker = queue.peekFirst();
			BigDecimal quantity = incoming.getRemaining().min(maker.getRemaining());

			maker.fill(quantity);
			incoming.fill(quantity);
			matches.add(new Match(
					maker.getOrderId(),
					maker.getUserId(),
					incoming.getOrderId(),
					incoming.getUserId(),
					incoming.getSide(),
					maker.getPrice(),
					quantity));

			if (maker.isFilled()) {
				queue.pollFirst();
				if (queue.isEmpty()) {
					opposite.pollFirstEntry();
				}
			}
		}

		if (!incoming.isFilled()) {
			add(incoming);
		}
		return matches;
	}

	private static boolean crosses(RestingOrder incoming, BigDecimal bestOppositePrice) {
		int comparison = incoming.getPrice().compareTo(bestOppositePrice);
		return incoming.getSide() == OrderSide.BUY ? comparison >= 0 : comparison <= 0;
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

	private NavigableMap<BigDecimal, Deque<RestingOrder>> oppositeOf(OrderSide side) {
		return side == OrderSide.BUY ? asks : bids;
	}
}
