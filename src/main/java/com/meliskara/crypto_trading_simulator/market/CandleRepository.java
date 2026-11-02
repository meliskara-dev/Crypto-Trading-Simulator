package com.meliskara.crypto_trading_simulator.market;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

interface CandleRepository extends JpaRepository<Candle, UUID> {

	Optional<Candle> findBySymbolAndTimeframeAndOpenTime(Symbol symbol, Timeframe timeframe, Instant openTime);
}
