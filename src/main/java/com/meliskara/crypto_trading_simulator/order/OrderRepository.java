package com.meliskara.crypto_trading_simulator.order;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface OrderRepository extends JpaRepository<Order, UUID> {

	Optional<Order> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);

	Optional<Order> findByIdAndUserId(UUID id, UUID userId);

	List<Order> findByUserIdOrderByCreatedAtDesc(UUID userId, Limit limit);
}
