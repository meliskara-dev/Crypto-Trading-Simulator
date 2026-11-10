package com.meliskara.crypto_trading_simulator.common.outbox;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

@Service
public class OutboxService {

	private final OutboxRepository outboxRepository;
	private final JsonMapper jsonMapper;

	OutboxService(OutboxRepository outboxRepository, JsonMapper jsonMapper) {
		this.outboxRepository = outboxRepository;
		this.jsonMapper = jsonMapper;
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void append(String topic, String messageKey, Object payload, Instant now) {
		String json = jsonMapper.writeValueAsString(payload);
		outboxRepository.save(new OutboxEvent(topic, messageKey, json, now));
	}
}