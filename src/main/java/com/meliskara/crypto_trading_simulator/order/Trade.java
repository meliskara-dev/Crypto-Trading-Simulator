package com.meliskara.crypto_trading_simulator.order;

import com.meliskara.crypto_trading_simulator.market.Symbol;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trades")
class Trade {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false, length = 20)
	private Symbol symbol;

	@Column(name = "buy_order_id", nullable = false, updatable = false)
	private UUID buyOrderId;

	@Column(name = "sell_order_id", nullable = false, updatable = false)
	private UUID sellOrderId;

	@Column(name = "buyer_id", nullable = false, updatable = false)
	private UUID buyerId;

	@Column(name = "seller_id", nullable = false, updatable = false)
	private UUID sellerId;

	@Enumerated(EnumType.STRING)
	@Column(name = "taker_side", nullable = false, updatable = false, length = 4)
	private OrderSide takerSide;

	@Column(nullable = false, updatable = false, precision = 38, scale = 18)
	private BigDecimal price;

	@Column(nullable = false, updatable = false, precision = 38, scale = 18)
	private BigDecimal quantity;

	@Column(name = "executed_at", nullable = false, updatable = false)
	private Instant executedAt;

	protected Trade() {
	}

	private Trade(Symbol symbol, Match match, Instant executedAt) {
		this.symbol = symbol;
		this.buyOrderId = match.buyOrderId();
		this.sellOrderId = match.sellOrderId();
		this.buyerId = match.buyerId();
		this.sellerId = match.sellerId();
		this.takerSide = match.takerSide();
		this.price = match.price();
		this.quantity = match.quantity();
		this.executedAt = executedAt;
	}

	static Trade of(Symbol symbol, Match match, Instant executedAt) {
		return new Trade(symbol, match, executedAt);
	}

	UUID getId() {
		return id;
	}
}
