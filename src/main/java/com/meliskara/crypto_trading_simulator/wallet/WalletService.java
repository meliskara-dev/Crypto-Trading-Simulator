package com.meliskara.crypto_trading_simulator.wallet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class WalletService {

	private static final BigDecimal DEMO_USDT = new BigDecimal("10000");

	private final BalanceRepository balanceRepository;

	public WalletService(BalanceRepository balanceRepository) {
		this.balanceRepository = balanceRepository;
	}

	@Transactional
	public void createInitialBalances(UUID userId) {
		List<Balance> balances = List.of(
			new Balance(userId, "USDT", DEMO_USDT),
			new Balance(userId, "BTC", BigDecimal.ZERO),
			new Balance(userId, "ETH", BigDecimal.ZERO),
			new Balance(userId, "SOL", BigDecimal.ZERO)
		);
		balanceRepository.saveAll(balances);
	}
}
