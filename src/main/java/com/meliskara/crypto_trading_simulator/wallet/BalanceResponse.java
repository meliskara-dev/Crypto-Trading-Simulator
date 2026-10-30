package com.meliskara.crypto_trading_simulator.wallet;

import java.math.BigDecimal;

public record BalanceResponse(
		String asset,
		String available,
		String locked
) {

	static BalanceResponse from(Balance balance){
		return new BalanceResponse(
				balance.getAsset(),
				format(balance.getAvailable()),
				format(balance.getLocked())
		);
	}

	private static String format(BigDecimal amount){
		return amount.stripTrailingZeros().toPlainString();
	}
}
