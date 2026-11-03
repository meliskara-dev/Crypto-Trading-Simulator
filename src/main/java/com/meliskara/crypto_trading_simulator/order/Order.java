package com.meliskara.crypto_trading_simulator.order;

import com.meliskara.crypto_trading_simulator.market.Symbol;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "user_id", nullable = false, updatable = false)
	private UUID userId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false, length = 20)
	private Symbol symbol;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false, length = 4)
	private OrderSide side;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false, length = 10)
	private OrderType type;

	@Column(updatable = false, precision = 38, scale = 18)
	private BigDecimal price;

	@Column(nullable = false, updatable = false, precision = 38, scale = 18)
	private BigDecimal quantity;

	@Column(name = "filled_quantity", nullable = false, precision = 38, scale = 18)
	private BigDecimal filledQuantity;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OrderStatus status;

	@Column(name = "idempotency_key", nullable = false, updatable = false, length = 64)
	private String idempotencyKey;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Version
	@Column(nullable = false)
	private long version;

	protected Order() {
	}

	Order(UUID userId, Symbol symbol, OrderSide side, OrderType type, BigDecimal price, BigDecimal quantity, String idempotencyKey, Instant now) {
		this.userId = userId;
		this.symbol = symbol;
		this.side = side;
		this.type = type;
		this.price = price;
		this.quantity = quantity;
		this.filledQuantity = BigDecimal.ZERO;
		this.status = OrderStatus.NEW;
		this.idempotencyKey = idempotencyKey;
		this.createdAt = now;
		this.updatedAt = now;
	}

	UUID getId() {
		return id;
	}

	UUID getUserId() {
		return userId;
	}

	Symbol getSymbol() {
		return symbol;
	}

	OrderSide getSide() {
		return side;
	}

	OrderType getType() {
		return type;
	}

	BigDecimal getPrice() {
		return price;
	}

	BigDecimal getQuantity() {
		return quantity;
	}

	BigDecimal getFilledQuantity() {
		return filledQuantity;
	}

	OrderStatus getStatus() {
		return status;
	}

	Instant getCreatedAt() {
		return createdAt;
	}

	Instant getUpdatedAt() {
		return updatedAt;
	}
}
