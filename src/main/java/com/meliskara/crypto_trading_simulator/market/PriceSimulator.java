package com.meliskara.crypto_trading_simulator.market;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
class PriceSimulator {

	private static final Logger log = LoggerFactory.getLogger(PriceSimulator.class);
	private static final int MAX_STEP_BASIS_POINTS = 50;
	private static final int PRICE_SCALE = 2;

	private final Map<Symbol, BigDecimal> prices = new EnumMap<>(Symbol.class);
	private final PriceTickPublisher publisher;

	PriceSimulator(PriceTickPublisher publisher) {
		this.publisher = publisher;
		for (Symbol symbol : Symbol.values()) {
			prices.put(symbol, symbol.initialPrice());
		}
	}

	@Scheduled(fixedRate = 1000)
	void tick() {
		Instant now = Instant.now();
		for (Symbol symbol : Symbol.values()) {
			int step = ThreadLocalRandom.current()
					.nextInt(-MAX_STEP_BASIS_POINTS, MAX_STEP_BASIS_POINTS + 1);
			BigDecimal price = nextPrice(prices.get(symbol), step);
			prices.put(symbol, price);

			PriceTick tick = new PriceTick(symbol, price, now);
			log.debug("Price tick: {}", tick);
			publisher.publish(tick);
		}
	}

	static BigDecimal nextPrice(BigDecimal current, int basisPoints) {
		BigDecimal change = BigDecimal.valueOf(basisPoints, 4);
		return current.multiply(BigDecimal.ONE.add(change)).setScale(PRICE_SCALE, RoundingMode.HALF_UP);
	}
}
