package com.meliskara.crypto_trading_simulator.auth;

public class EmailAlreadyTakenException extends RuntimeException {

	public EmailAlreadyTakenException(String email) {
		super("Email already registered: " + email);
	}
}
