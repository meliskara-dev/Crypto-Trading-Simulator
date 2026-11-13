package com.meliskara.crypto_trading_simulator.order;

import com.meliskara.crypto_trading_simulator.market.Symbol;

import java.time.Instant;
import java.util.UUID;

import static com.meliskara.crypto_trading_simulator.common.Amounts.format;

record OrderResponse(
		UUID id,
		Symbol symbol,
		OrderSide side,
		OrderType type,
		String price,
		String quantity,
		String filledQuantity,
		OrderStatus status,
		Instant createdAt
) {

	static OrderResponse from(Order order) {
		return new OrderResponse(
				order.getId(),
				order.getSymbol(),
				order.getSide(),
				order.getType(),
				format(order.getPrice()),
				format(order.getQuantity()),
				format(order.getFilledQuantity()),
				order.getStatus(),
				order.getCreatedAt()
		);
	}
}
