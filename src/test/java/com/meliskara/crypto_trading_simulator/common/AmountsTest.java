package com.meliskara.crypto_trading_simulator.common;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AmountsTest {

	@Test
	void removesTrailingZerosFromDatabaseScale() {
		assertThat(Amounts.format(new BigDecimal("0.100000000000000000"))).isEqualTo("0.1");
	}

	@Test
	void zeroWithScaleBecomesPlainZero() {
		assertThat(Amounts.format(new BigDecimal("0E-18"))).isEqualTo("0");
	}

	@Test
	void wholeNumberIsNotWrittenInScientificNotation() {
		assertThat(Amounts.format(new BigDecimal("60000.00"))).isEqualTo("60000");
	}

	@Test
	void nullStaysNull() {
		assertThat(Amounts.format(null)).isNull();
	}
}