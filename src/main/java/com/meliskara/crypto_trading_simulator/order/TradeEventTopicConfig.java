package com.meliskara.crypto_trading_simulator.order;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
class TradeEventTopicConfig {

	static final String TRADE_EVENTS_TOPIC = "trade-events";

	@Bean
	NewTopic tradeEventsTopic() {
		return TopicBuilder.name(TRADE_EVENTS_TOPIC)
				.partitions(3)
				.replicas(1)
				.build();
	}
}