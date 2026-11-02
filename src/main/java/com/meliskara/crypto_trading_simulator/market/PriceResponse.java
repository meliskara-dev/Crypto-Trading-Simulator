package com.meliskara.crypto_trading_simulator.market;

import java.math.BigDecimal;

record PriceResponse(String symbol, String price) {

	static PriceResponse of(Symbol symbol, BigDecimal price) {
		return new PriceResponse(symbol.name(),
				price.stripTrailingZeros().toPlainString());
	}
}
