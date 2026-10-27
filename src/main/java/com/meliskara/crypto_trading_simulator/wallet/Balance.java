package com.meliskara.crypto_trading_simulator.wallet;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "balances")
public class Balance {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "user_id", nullable = false, updatable = false)
	private UUID userId;

	@Column(nullable = false, updatable = false, length = 10)
	private String asset;

	@Column(nullable = false, precision = 38, scale = 18)
	private BigDecimal available;

	@Column(nullable = false, precision = 38, scale = 18)
	private BigDecimal locked;

	@Version
	@Column(nullable = false)
	private long version;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Balance(){
	}

	public Balance(UUID userId, String asset, BigDecimal available) {
		this.userId = userId;
		this.asset = asset;
		this.available = available;
		this.locked = BigDecimal.ZERO;
		this.updatedAt = Instant.now();
	}

	public UUID getId(){ return id; }
	public UUID getUserId(){ return userId; }
	public String getAsset(){ return asset; }
	public BigDecimal getAvailable(){ return available; }
	public BigDecimal getLocked(){ return locked; }
	public Instant getUpdatedAt(){ return updatedAt; }
}
