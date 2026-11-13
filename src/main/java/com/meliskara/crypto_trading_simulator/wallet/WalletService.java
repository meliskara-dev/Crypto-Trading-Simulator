package com.meliskara.crypto_trading_simulator.wallet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class WalletService {

	private static final BigDecimal DEMO_USDT = new BigDecimal("10000");
	private static final BigDecimal DEMO_BTC = new BigDecimal("1");
	private static final BigDecimal DEMO_ETH = new BigDecimal("10");
	private static final BigDecimal DEMO_SOL = new BigDecimal("100");

	private final BalanceRepository balanceRepository;

	public WalletService(BalanceRepository balanceRepository) {
		this.balanceRepository = balanceRepository;
	}

	@Transactional
	public void createInitialBalances(UUID userId) {
		List<Balance> balances = List.of(
				new Balance(userId, "USDT", DEMO_USDT),
				new Balance(userId, "BTC", DEMO_BTC),
				new Balance(userId, "ETH", DEMO_ETH),
				new Balance(userId, "SOL", DEMO_SOL)
		);
		balanceRepository.saveAll(balances);
	}

	@Transactional(readOnly = true)
	public List<BalanceResponse> getBalances(UUID userId){
		return balanceRepository.findByUserIdOrderByAsset(userId).stream()
				.map(BalanceResponse::from)
				.toList();
	}

	@Transactional
	public void lock(UUID userId, String asset, BigDecimal amount, Instant now) {
		findBalance(userId, asset).lock(amount, now);
	}

	@Transactional
	public void unlock(UUID userId, String asset, BigDecimal amount, Instant now) {
		findBalance(userId, asset).unlock(amount, now);
	}

	@Transactional
	public void transferLocked(UUID fromUserId, UUID toUserId, String asset, BigDecimal amount, Instant now) {
		findBalance(fromUserId, asset).debitLocked(amount, now);
		findBalance(toUserId, asset).credit(amount, now);
	}

	private Balance findBalance(UUID userId, String asset) {
		return balanceRepository.findByUserIdAndAsset(userId, asset)
				.orElseThrow(() -> new IllegalStateException("Balance not found: " + asset));
	}
}
