package com.meliskara.crypto_trading_simulator.market;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
class CandleListener {

	private final CandleService candleService;

	CandleListener(CandleService candleService) {
		this.candleService = candleService;
	}

	@KafkaListener(topics = PriceTickTopicConfig.PRICE_TICKS_TOPIC, groupId = "candle-aggregator")
	void onTick(PriceTick tick) {
		candleService.recordTick(tick);
	}
}
