package com.meliskara.crypto_trading_simulator.order;

import com.meliskara.crypto_trading_simulator.market.Symbol;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
class MatchingEngine {

	private final Map<Symbol, OrderBook> books =
			new EnumMap<>(Symbol.class);
	private final Map<Symbol, ExecutorService> executors =
			new EnumMap<>(Symbol.class);
	private static final Logger log =
			LoggerFactory.getLogger(MatchingEngine.class);

	MatchingEngine() {
		for (Symbol symbol : Symbol.values()) {
			books.put(symbol, new OrderBook(symbol));
			executors.put(symbol, Executors.newSingleThreadExecutor(
					Thread.ofPlatform().name("matching-" + symbol.name()).factory()));
		}
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	void onOrderPlaced(OrderPlacedEvent event) {
		submit(event.symbol(), event.toRestingOrder());
	}

	void submit(Symbol symbol, RestingOrder order) {
		executors.get(symbol).execute(() -> process(symbol, order));
	}

	private void process(Symbol symbol, RestingOrder order) {
		try {
			List<Match> matches = books.get(symbol).match(order);
			if (!matches.isEmpty()) {
				log.info("{} order {} produced {} match(es)", symbol, order.getOrderId(), matches.size());
			}
		} catch (RuntimeException ex) {
			log.error("Matching failed for {} order {}", symbol, order.getOrderId(), ex);
		}
	}

	@PreDestroy
	void shutdown() {
		executors.values().forEach(ExecutorService::shutdown);
	}
}
