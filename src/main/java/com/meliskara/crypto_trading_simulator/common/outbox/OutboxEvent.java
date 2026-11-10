package com.meliskara.crypto_trading_simulator.common.outbox;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
class OutboxEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, updatable = false, length = 100)
	private String topic;

	@Column(name = "message_key", nullable = false, updatable = false, length = 100)
	private String messageKey;

	@Column(nullable = false, updatable = false)
	private String payload;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "published_at")
	private Instant publishedAt;

	protected OutboxEvent() {
	}

	OutboxEvent(String topic, String messageKey, String payload, Instant now) {
		this.topic = topic;
		this.messageKey = messageKey;
		this.payload = payload;
		this.createdAt = now;
	}

	void markPublished(Instant now) {
		this.publishedAt = now;
	}

	UUID getId() {
		return id;
	}

	String getTopic() {
		return topic;
	}

	String getMessageKey() {
		return messageKey;
	}

	String getPayload() {
		return payload;
	}
}