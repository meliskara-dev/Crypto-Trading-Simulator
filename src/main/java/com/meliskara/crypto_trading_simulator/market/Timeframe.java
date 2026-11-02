package com.meliskara.crypto_trading_simulator.market;

import java.time.Duration;
import java.time.Instant;

enum Timeframe {

	ONE_MINUTE("1m", Duration.ofMinutes(1)),
	FIVE_MINUTES("5m", Duration.ofMinutes(5)),
	ONE_HOUR("1h", Duration.ofHours(1));

	private final String code;
	private final Duration duration;

	Timeframe(String code, Duration duration) {
		this.code = code;
		this.duration = duration;
	}

	String code() {
		return code;
	}

	static Timeframe fromCode(String code) {
		for (Timeframe timeframe : Timeframe.values()) {
			if (timeframe.code.equals(code)) {
				return timeframe;
			}
		}
		throw new IllegalArgumentException("Unknown timeframe: " + code);
	}

	Instant openTimeOf(Instant timestamp) {
		long epochMillis = timestamp.toEpochMilli();
		long durationMillis = duration.toMillis();
		return Instant.ofEpochMilli(epochMillis - Math.floorMod(epochMillis, durationMillis));
	}
}
