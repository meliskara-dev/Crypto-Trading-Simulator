package com.meliskara.crypto_trading_simulator.order;

import java.math.BigDecimal;
import java.util.UUID;

final class RestingOrder {

	private final UUID orderId;
	private final UUID userId;
	private final OrderSide side;
	private final BigDecimal price;
	private BigDecimal remaining;

	RestingOrder(UUID orderId, UUID userId, OrderSide side, BigDecimal price, BigDecimal remaining) {
		if(price.signum() <= 0 || remaining.signum() <= 0)
		{
			throw new IllegalArgumentException("Price and remaining quantity must be positive");
		}
		this.orderId = orderId;
		this.userId = userId;
		this.side = side;
		this.price = price;
		this.remaining = remaining;
	}

	void fill(BigDecimal quantity) {
		if (quantity.signum() <= 0 || quantity.compareTo(remaining) > 0) {
			throw new IllegalArgumentException("Invalid fill quantity: " + quantity);
		}
		remaining = remaining.subtract(quantity);
	}

	boolean isFilled() {
		return remaining.signum() == 0;
	}

	UUID getOrderId() {
		return orderId;
	}

	UUID getUserId() {
		return userId;
	}

	OrderSide getSide() {
		return side;
	}

	BigDecimal getPrice() {
		return price;
	}

	BigDecimal getRemaining() {
		return remaining;
	}
}
