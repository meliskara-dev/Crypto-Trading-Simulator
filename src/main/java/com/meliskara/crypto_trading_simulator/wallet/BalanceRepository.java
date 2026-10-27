package com.meliskara.crypto_trading_simulator.wallet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface BalanceRepository extends JpaRepository<Balance, UUID> {

	List<Balance> findByUserId(UUID userId);
}
