package com.meliskara.crypto_trading_simulator.market;

import java.math.BigDecimal;

import static com.meliskara.crypto_trading_simulator.common.Amounts.format;

record PriceResponse(String symbol, String price) {

	static PriceResponse of(Symbol symbol, BigDecimal price) {
		return new PriceResponse(symbol.name(),
				format(price));
	}
}
