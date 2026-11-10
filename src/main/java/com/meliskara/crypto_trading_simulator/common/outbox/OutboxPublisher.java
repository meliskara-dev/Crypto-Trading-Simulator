package com.meliskara.crypto_trading_simulator.common.outbox;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
class OutboxPublisher {

	private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

	private final OutboxRepository outboxRepository;
	private final KafkaTemplate<String, String> kafkaTemplate;

	OutboxPublisher(OutboxRepository outboxRepository, ProducerFactory<String, String> producerFactory) {
		this.outboxRepository = outboxRepository;
		this.kafkaTemplate = new KafkaTemplate<>(producerFactory,
				Map.of(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class));
	}

	@Scheduled(fixedDelay = 1000)
	@Transactional
	void publishPending() {
		for (OutboxEvent event : outboxRepository.findTop100ByPublishedAtIsNullOrderByCreatedAtAsc()) {
			try {
				kafkaTemplate.send(event.getTopic(), event.getMessageKey(), event.getPayload())
						.get(5, TimeUnit.SECONDS);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				return;
			} catch (ExecutionException | TimeoutException ex) {
				log.warn("Outbox publish failed for event {}, will retry", event.getId(), ex);
				return;
			}
			event.markPublished(Instant.now());
		}
	}
}