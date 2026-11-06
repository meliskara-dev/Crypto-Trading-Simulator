package com.meliskara.crypto_trading_simulator.order;

import java.math.BigDecimal;
import java.util.UUID;

record Match(
		UUID makerOrderId,
		UUID makerUserId,
		UUID takerOrderId,
		UUID takerUserId,
		OrderSide takerSide,
		BigDecimal price,
		BigDecimal quantity
) {

	Match {
		if (price.signum() <= 0 || quantity.signum() <= 0) {
			throw new IllegalArgumentException("Match price and quantity must be positive");
		}
	}
}
