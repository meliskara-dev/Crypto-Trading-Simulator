package com.meliskara.crypto_trading_simulator.common;

import java.math.BigDecimal;

public final class Amounts {

	private Amounts() {
	}

	public static String format(BigDecimal value) {
		return value == null ? null : value.stripTrailingZeros().toPlainString();
	}
}