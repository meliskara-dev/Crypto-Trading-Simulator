package com.meliskara.crypto_trading_simulator.market;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
class PriceTickPublisher {

	private static final Logger log = LoggerFactory.getLogger(PriceTickPublisher.class);

	private final KafkaTemplate<String, PriceTick> kafkaTemplate;

	PriceTickPublisher(KafkaTemplate<String, PriceTick> kafkaTemplate){
		this.kafkaTemplate = kafkaTemplate;
	}

	void publish(PriceTick tick) {
		kafkaTemplate.send(PriceTickTopicConfig.PRICE_TICKS_TOPIC, tick.symbol().name(), tick)
				.whenComplete((result, ex) -> {
					if (ex != null) {
						log.warn("Failed to publish price tick: {}", tick, ex);
					}
				});
	}
}
