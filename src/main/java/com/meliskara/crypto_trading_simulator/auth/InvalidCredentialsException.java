package com.meliskara.crypto_trading_simulator.auth;

public class InvalidCredentialsException extends RuntimeException{

	public InvalidCredentialsException(){
		super("Invalid email or password");
	}
}
