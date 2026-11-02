package com.meliskara.crypto_trading_simulator.market;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
class LastPriceCache {

	private static final String KEY_PREFIX = "price:last:";

	private final StringRedisTemplate redisTemplate;

	LastPriceCache(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	void put(PriceTick tick) {
		redisTemplate.opsForValue().set(key(tick.symbol()), tick.price().toPlainString());
	}

	Optional<BigDecimal> find(Symbol symbol) {
		String value = redisTemplate.opsForValue().get(key(symbol));
		return Optional.ofNullable(value).map(BigDecimal::new);
	}

	private static String key(Symbol symbol) {
		return KEY_PREFIX + symbol.name();
	}
}
