package com.meliskara.crypto_trading_simulator.order;

import com.meliskara.crypto_trading_simulator.market.Symbol;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

record CreateOrderRequest(
    @NotNull Symbol symbol,
	@NotNull OrderSide side,
	@NotNull OrderType type,
	@Positive @Digits(integer = 12, fraction = 2)
	BigDecimal price,
	@NotNull @Positive @Digits(integer = 12, fraction = 8)
	BigDecimal quantity
) {

	@AssertTrue(message = "price is required for LIMIT orders")
	boolean isPriceProvidedForLimit() {
		return type != OrderType.LIMIT || price != null;
	}
}
