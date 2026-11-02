package com.meliskara.crypto_trading_simulator.market;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
class LastPriceListener {

	private final LastPriceCache lastPriceCache;

	LastPriceListener(LastPriceCache lastPriceCache) {
		this.lastPriceCache = lastPriceCache;
	}

	@KafkaListener(topics = PriceTickTopicConfig.PRICE_TICKS_TOPIC, groupId = "last-price-cache")
	void onTick(PriceTick tick) {
		lastPriceCache.put(tick);
	}
}
