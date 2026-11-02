package com.meliskara.crypto_trading_simulator.market;

import java.math.BigDecimal;
import java.time.Instant;

record CandleResponse(
    Instant openTime,
    String open,
    String high,
    String low,
    String close
) {

	static CandleResponse from(Candle candle) {
		return new CandleResponse(
				candle.getOpenTime(),
				format(candle.getOpen()),
				format(candle.getHigh()),
				format(candle.getLow()),
				format(candle.getClose())
		);
	}

	private static String format(BigDecimal value) {
		return value.stripTrailingZeros().toPlainString();
	}
}
