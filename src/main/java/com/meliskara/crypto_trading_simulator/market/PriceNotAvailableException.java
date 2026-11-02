package com.meliskara.crypto_trading_simulator.market;

public class PriceNotAvailableException extends RuntimeException {

	public PriceNotAvailableException(Symbol symbol)
	{
		super("No price available yet for " + symbol.name());
	}
}
