package com.meliskara.crypto_trading_simulator.order;

import com.meliskara.crypto_trading_simulator.market.Symbol;

import java.math.BigDecimal;
import java.util.UUID;

record OrderPlacedEvent(
		UUID orderId,
		UUID userId,
		Symbol symbol,
		OrderSide side,
		BigDecimal price,
		BigDecimal quantity
) {

	static OrderPlacedEvent from(Order order) {
		return new OrderPlacedEvent(
				order.getId(),
				order.getUserId(),
				order.getSymbol(),
				order.getSide(),
				order.getPrice(),
				order.getQuantity());
	}

	RestingOrder toRestingOrder() {
		return new RestingOrder(orderId, userId, side, price, quantity);
	}
}
