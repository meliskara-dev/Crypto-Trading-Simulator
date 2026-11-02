package com.meliskara.crypto_trading_simulator.market;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "candles")
class Candle {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false, length = 20)
	private Symbol symbol;

	@Column(nullable = false, updatable = false, length = 5)
	private Timeframe timeframe;

	@Column(name = "open_time", nullable = false, updatable = false)
	private Instant openTime;

	@Column(nullable = false, updatable = false, precision = 38, scale = 18)
	private BigDecimal open;

	@Column(nullable = false, precision = 38, scale = 18)
	private BigDecimal high;

	@Column(nullable = false, precision = 38, scale = 18)
	private BigDecimal low;

	@Column(nullable = false, precision = 38, scale = 18)
	private BigDecimal close;

	protected Candle() {

	}

	Candle(Symbol symbol, Timeframe timeframe, Instant openTime, BigDecimal price) {
		this.symbol = symbol;
		this.timeframe = timeframe;
		this.openTime = openTime;
		this.open = price;
		this.high = price;
		this.low = price;
		this.close = price;
	}

	void apply(BigDecimal price) {
		this.high = high.max(price);
		this.low = low.min(price);
		this.close = price;
	}

	Symbol getSymbol() {
		return symbol;
	}

	Timeframe getTimeframe() {
		return timeframe;
	}

	Instant getOpenTime() {
		return openTime;
	}

	BigDecimal getOpen() {
		return open;
	}

	BigDecimal getHigh() {
		return high;
	}

	BigDecimal getLow() {
		return low;
	}

	BigDecimal getClose() {
		return close;
	}
}
