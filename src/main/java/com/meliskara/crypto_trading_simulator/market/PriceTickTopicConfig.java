package com.meliskara.crypto_trading_simulator.market;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
class PriceTickTopicConfig {
	static final String PRICE_TICKS_TOPIC = "price-ticks";

	@Bean
	NewTopic priceTicksTopic() {
		return TopicBuilder.name(PRICE_TICKS_TOPIC)
				.partitions(3)
				.replicas(1)
				.build();
	}
}
