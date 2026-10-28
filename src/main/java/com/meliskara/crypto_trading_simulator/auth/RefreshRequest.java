package com.meliskara.crypto_trading_simulator.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest (
		@NotBlank String refreshToken
){
}
