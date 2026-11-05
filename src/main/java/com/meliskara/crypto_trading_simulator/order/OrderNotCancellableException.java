package com.meliskara.crypto_trading_simulator.order;

import java.util.UUID;

public class OrderNotCancellableException extends RuntimeException {

	public OrderNotCancellableException(UUID orderId, OrderStatus status) {
		super("Order " + orderId + " cannot be cancelled in status " + status);
	}
}
