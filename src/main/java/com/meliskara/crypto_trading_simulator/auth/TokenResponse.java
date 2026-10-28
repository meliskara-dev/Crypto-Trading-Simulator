package com.meliskara.crypto_trading_simulator.auth;

public record TokenResponse(
		String accessToken,
		String refreshToken,
		String tokenType,
		long expiresIn
){
}
