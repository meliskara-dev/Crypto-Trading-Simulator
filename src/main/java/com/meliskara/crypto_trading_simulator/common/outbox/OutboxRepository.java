package com.meliskara.crypto_trading_simulator.common.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {

	List<OutboxEvent> findTop100ByPublishedAtIsNullOrderByCreatedAtAsc();
}