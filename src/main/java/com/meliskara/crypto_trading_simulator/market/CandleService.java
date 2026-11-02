package com.meliskara.crypto_trading_simulator.market;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
class CandleService {

	private final CandleRepository candleRepository;

	CandleService(CandleRepository candleRepository) {
		this.candleRepository = candleRepository;
	}

	@Transactional
	void recordTick(PriceTick tick) {
		for (Timeframe timeframe : Timeframe.values()) {
			Instant openTime = timeframe.openTimeOf(tick.timestamp());

			candleRepository.findBySymbolAndTimeframeAndOpenTime(tick.symbol(), timeframe, openTime)
					.ifPresentOrElse(
							candle -> candle.apply(tick.price()),
							() -> candleRepository.save(new Candle(tick.symbol(), timeframe, openTime, tick.price()))
					);
		}
	}
}
