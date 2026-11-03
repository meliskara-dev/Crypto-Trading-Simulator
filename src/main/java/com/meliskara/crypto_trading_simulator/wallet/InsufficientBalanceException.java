package com.meliskara.crypto_trading_simulator.wallet;

public class InsufficientBalanceException extends RuntimeException {

	public InsufficientBalanceException(String asset) {
		super("Insufficient " + asset + " balance");
	}
}
