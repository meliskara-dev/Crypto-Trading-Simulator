package com.meliskara.crypto_trading_simulator.market;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Limit;

@Service
public class MarketService {

	private final LastPriceCache lastPriceCache;
	private final CandleRepository candleRepository;

	MarketService(LastPriceCache lastPriceCache, CandleRepository candleRepository) {
		this.lastPriceCache = lastPriceCache;
		this.candleRepository = candleRepository;
	}

	public BigDecimal getLastPrice(Symbol symbol) {
		return lastPriceCache.find(symbol)
				.orElseThrow(() -> new PriceNotAvailableException(symbol));
	}

	@Transactional(readOnly = true)
	List<CandleResponse> getCandles(Symbol symbol, Timeframe timeframe, int limit) {
		return candleRepository.findBySymbolAndTimeframeOrderByOpenTimeDesc(symbol, timeframe, Limit.of(limit))
				.stream()
				.map(CandleResponse::from)
				.toList()
				.reversed();
	}

	@Transactional(readOnly = true)
	Stats24hResponse getStats24h(Symbol symbol, Instant now) {
		BigDecimal last = getLastPrice(symbol);
		Instant from = Timeframe.ONE_HOUR.openTimeOf(now.minus(Duration.ofHours(24)));

		List<Candle> candles = candleRepository
				.findBySymbolAndTimeframeAndOpenTimeGreaterThanEqualOrderByOpenTimeAsc(symbol, Timeframe.ONE_HOUR, from);
		if (candles.isEmpty()) {
			throw new PriceNotAvailableException(symbol);
		}

		BigDecimal open = candles.getFirst().getOpen();
		BigDecimal high = candles.stream().map(Candle::getHigh).reduce(last, BigDecimal::max);
		BigDecimal low = candles.stream().map(Candle::getLow).reduce(last, BigDecimal::min);
		BigDecimal changePercent = last.subtract(open)
				.multiply(BigDecimal.valueOf(100))
				.divide(open, 2, BigDecimal.ROUND_HALF_UP);

		return new Stats24hResponse(
				symbol.name(),
				format(open),
				format(high),
				format(low),
				format(last),
				changePercent.toPlainString()
		);
	}

	private static String format(BigDecimal value) {
		return value.stripTrailingZeros().toPlainString();
	}
}
