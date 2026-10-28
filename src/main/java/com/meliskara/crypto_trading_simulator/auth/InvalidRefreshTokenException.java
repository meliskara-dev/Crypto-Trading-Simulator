package com.meliskara.crypto_trading_simulator.auth;

public class InvalidRefreshTokenException extends RuntimeException{

	public InvalidRefreshTokenException(){
		super("Invalid or expired refresh token");
	}
}
