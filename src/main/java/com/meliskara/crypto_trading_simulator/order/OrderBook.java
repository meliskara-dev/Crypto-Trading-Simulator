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
	private final Map<UUID, RestingOrder> ordersById = new HashMap<>();

	OrderBook(Symbol symbol) {
		this.symbol = symbol;
	}

	void add(RestingOrder order) {
		if (ordersById.putIfAbsent(order.getOrderId(), order) != null) {
			throw new IllegalStateException("Order already in book: " + order.getOrderId());
		}
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
				ordersById.remove(maker.getOrderId());
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

	boolean remove(UUID orderId) {
		RestingOrder order = ordersById.remove(orderId);
		if (order == null) {
			return false;
		}
		NavigableMap<BigDecimal, Deque<RestingOrder>> side =
				sideOf(order.getSide());
		Deque<RestingOrder> queue = side.get(order.getPrice());
		queue.remove(order);
		if (queue.isEmpty()) {
			side.remove(order.getPrice());
		}
		return true;
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
