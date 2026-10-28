package com.meliskara.crypto_trading_simulator.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest (
		@NotBlank @Email String email,
		@NotBlank String password
){
}
