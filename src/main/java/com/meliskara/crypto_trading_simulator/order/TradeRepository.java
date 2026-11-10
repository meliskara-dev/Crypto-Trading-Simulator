package com.meliskara.crypto_trading_simulator.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface TradeRepository extends JpaRepository<Trade, UUID> {
}
