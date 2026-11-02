package com.meliskara.crypto_trading_simulator.market;

import java.math.BigDecimal;
import java.time.Instant;

public record PriceTick(Symbol symbol, BigDecimal price, Instant timestamp) {
}
