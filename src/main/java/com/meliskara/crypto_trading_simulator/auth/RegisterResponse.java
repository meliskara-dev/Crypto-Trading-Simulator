package com.meliskara.crypto_trading_simulator.auth;

import java.util.UUID;

public record RegisterResponse (UUID id, String email) {
}
