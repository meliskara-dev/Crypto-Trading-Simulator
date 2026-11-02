package com.meliskara.crypto_trading_simulator.market;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface CandleRepository extends JpaRepository<Candle, UUID> {

	Optional<Candle> findBySymbolAndTimeframeAndOpenTime(Symbol symbol, Timeframe timeframe, Instant openTime);

	List<Candle> findBySymbolAndTimeframeOrderByOpenTimeDesc(Symbol symbol, Timeframe timeframe, Limit limit);

	List<Candle> findBySymbolAndTimeframeAndOpenTimeGreaterThanEqualOrderByOpenTimeAsc(Symbol symbol, Timeframe timeframe, Instant from);
}
