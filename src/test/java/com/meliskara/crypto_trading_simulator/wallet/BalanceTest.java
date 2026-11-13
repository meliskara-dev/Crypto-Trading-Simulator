package com.meliskara.crypto_trading_simulator.wallet;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BalanceTest {

	private static final Instant NOW = Instant.parse("2026-11-13T10:00:00Z");

	@Test
	void lockMovesFromAvailableToLockedAndKeepsTotal() {
		Balance balance = usdt("10000");

		balance.lock(new BigDecimal("6000"), NOW);

		assertThat(balance.getAvailable()).isEqualByComparingTo("4000");
		assertThat(balance.getLocked()).isEqualByComparingTo("6000");
		assertThat(balance.getAvailable().add(balance.getLocked())).isEqualByComparingTo("10000");
		assertThat(balance.getUpdatedAt()).isEqualTo(NOW);
	}

	@Test
	void lockMoreThanAvailableThrowsAndChangesNothing() {
		Balance balance = usdt("100");

		assertThatThrownBy(() -> balance.lock(new BigDecimal("100.01"), NOW))
				.isInstanceOf(InsufficientBalanceException.class);

		assertThat(balance.getAvailable()).isEqualByComparingTo("100");
		assertThat(balance.getLocked()).isEqualByComparingTo("0");
	}

	@Test
	void lockExactlyAvailableIsAllowed() {
		Balance balance = usdt("100");

		balance.lock(new BigDecimal("100"), NOW);

		assertThat(balance.getAvailable()).isEqualByComparingTo("0");
	}

	@Test
	void unlockMoreThanLockedThrows() {
		Balance balance = usdt("100");
		balance.lock(new BigDecimal("10"), NOW);

		assertThatThrownBy(() -> balance.unlock(new BigDecimal("11"), NOW))
				.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void debitLockedReducesOnlyLockedAndCreditIncreasesOnlyAvailable() {
		Balance seller = usdt("0");
		Balance buyer = usdt("6000");
		buyer.lock(new BigDecimal("6000"), NOW);

		buyer.debitLocked(new BigDecimal("6000"), NOW);
		seller.credit(new BigDecimal("6000"), NOW);

		assertThat(buyer.getAvailable()).isEqualByComparingTo("0");
		assertThat(buyer.getLocked()).isEqualByComparingTo("0");
		assertThat(seller.getAvailable()).isEqualByComparingTo("6000");
	}

	@Test
	void zeroOrNegativeAmountIsRejected() {
		Balance balance = usdt("100");

		assertThatThrownBy(() -> balance.lock(BigDecimal.ZERO, NOW))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> balance.credit(new BigDecimal("-1"), NOW))
				.isInstanceOf(IllegalArgumentException.class);
	}

	private static Balance usdt(String available) {
		return new Balance(UUID.randomUUID(), "USDT", new BigDecimal(available));
	}
}