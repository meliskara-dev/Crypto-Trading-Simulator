package com.meliskara.crypto_trading_simulator.order;

public class UnsupportedOrderTypeException extends RuntimeException {

	public UnsupportedOrderTypeException(OrderType type) {
		super(type + " orders are not supported yet");
	}
}
