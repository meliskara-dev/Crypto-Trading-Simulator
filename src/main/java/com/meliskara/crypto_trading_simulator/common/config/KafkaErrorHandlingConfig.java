package com.meliskara.crypto_trading_simulator.common.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
class KafkaErrorHandlingConfig {

	@Bean
	DefaultErrorHandler kafkaErrorHandler(ProducerFactory<Object, Object> producerFactory,
										  KafkaTemplate<Object, Object> jsonTemplate) {
		KafkaTemplate<Object, Object> bytesTemplate = new KafkaTemplate<>(producerFactory,
				Map.of(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class));

		Map<Class<?>, KafkaOperations<?, ?>> templates = new LinkedHashMap<>();
		templates.put(byte[].class, bytesTemplate);
		templates.put(Object.class, jsonTemplate);

		DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(templates,
				(record, ex) -> new TopicPartition(record.topic() + ".DLT", -1));

		return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2));
	}
}