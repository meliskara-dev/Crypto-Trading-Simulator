package com.meliskara.crypto_trading_simulator.market;

import java.math.BigDecimal;

public enum Symbol {
    BTCUSDT("BTC", new BigDecimal("65000")),
    ETHUSDT("ETH", new BigDecimal("3500")),
    SOLUSDT("SOL", new BigDecimal("150"));

	private final String baseAsset;
	private final BigDecimal initialPrice;

	Symbol(String baseAsset, BigDecimal initialPrice) {
		this.baseAsset = baseAsset;
		this.initialPrice = initialPrice;
	}

	public String baseAsset() {
		return baseAsset;
	}

	public BigDecimal initialPrice() {
		return initialPrice;
	}
}
