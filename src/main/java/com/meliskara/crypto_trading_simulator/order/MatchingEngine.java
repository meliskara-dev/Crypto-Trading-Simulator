package com.meliskara.crypto_trading_simulator.order;

import com.meliskara.crypto_trading_simulator.market.Symbol;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
class MatchingEngine {

	private static final Logger log = LoggerFactory.getLogger(MatchingEngine.class);

	private final Map<Symbol, OrderBook> books = new EnumMap<>(Symbol.class);
	private final Map<Symbol, ExecutorService> executors = new EnumMap<>(Symbol.class);
	private final SettlementService settlementService;
	private final OrderService orderService;

	MatchingEngine(SettlementService settlementService, OrderService orderService) {
		this.settlementService = settlementService;
		this.orderService = orderService;
		for (Symbol symbol : Symbol.values()) {
			books.put(symbol, new OrderBook(symbol));
			executors.put(symbol, Executors.newSingleThreadExecutor(
					Thread.ofPlatform().name("matching-" + symbol.name()).factory()));
		}
	}

	@EventListener(ApplicationReadyEvent.class)
	void rebuildAll() {
		for (Symbol symbol : Symbol.values()) {
			executors.get(symbol).execute(() -> rebuild(symbol));
		}
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	void onOrderPlaced(OrderPlacedEvent event) {
		executors.get(event.symbol()).execute(() -> process(event.symbol(), event.toRestingOrder()));
	}

	private void process(Symbol symbol, RestingOrder order) {
		OrderBook book = books.get(symbol);
		if (book.contains(order.getOrderId())) {
			return;
		}
		try {
			matchAndSettle(symbol, book, order);
		} catch (RuntimeException ex) {
			log.error("Settlement failed for {} order {}, rebuilding book", symbol, order.getOrderId(), ex);
			rebuild(symbol);
		}
	}

	private void rebuild(Symbol symbol) {
		OrderBook book = new OrderBook(symbol);
		books.put(symbol, book);
		List<RestingOrder> openOrders = orderService.findOpenOrders(symbol);
		for (RestingOrder order : openOrders) {
			try {
				matchAndSettle(symbol, book, order);
			} catch (RuntimeException ex) {
				log.error("Rebuild failed for {} order {}", symbol, order.getOrderId(), ex);
			}
		}
		log.info("Rebuilt {} book from {} open order(s)", symbol, openOrders.size());
	}

	private void matchAndSettle(Symbol symbol, OrderBook book, RestingOrder order) {
		List<Match> matches = book.match(order);
		if (!matches.isEmpty()) {
			settlementService.settle(symbol, matches, Instant.now());
		}
	}

	@PreDestroy
	void shutdown() {
		executors.values().forEach(ExecutorService::shutdown);
	}
}