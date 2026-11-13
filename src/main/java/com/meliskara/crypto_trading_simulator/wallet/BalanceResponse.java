package com.meliskara.crypto_trading_simulator.wallet;

import static com.meliskara.crypto_trading_simulator.common.Amounts.format;

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
}
