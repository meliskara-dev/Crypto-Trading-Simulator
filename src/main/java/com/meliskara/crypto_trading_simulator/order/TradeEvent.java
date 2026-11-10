package com.meliskara.crypto_trading_simulator.order;

import com.meliskara.crypto_trading_simulator.market.Symbol;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TradeEvent(
		UUID tradeId,
		Symbol symbol,
		UUID buyOrderId,
		UUID sellOrderId,
		UUID buyerId,
		UUID sellerId,
		OrderSide takerSide,
		BigDecimal price,
		BigDecimal quantity,
		Instant executedAt
) {

	static TradeEvent of(UUID tradeId, Symbol symbol, Match match, Instant executedAt) {
		return new TradeEvent(tradeId, symbol, match.buyOrderId(), match.sellOrderId(),
				match.buyerId(), match.sellerId(), match.takerSide(),
				match.price(), match.quantity(), executedAt);
	}
}