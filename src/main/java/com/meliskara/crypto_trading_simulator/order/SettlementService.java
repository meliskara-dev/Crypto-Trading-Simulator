package com.meliskara.crypto_trading_simulator.order;

import com.meliskara.crypto_trading_simulator.market.Symbol;
import com.meliskara.crypto_trading_simulator.wallet.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
class SettlementService {

	private final OrderRepository orderRepository;
	private final TradeRepository tradeRepository;
	private final WalletService walletService;

	SettlementService(OrderRepository orderRepository, TradeRepository tradeRepository, WalletService walletService) {
		this.orderRepository = orderRepository;
		this.tradeRepository = tradeRepository;
		this.walletService = walletService;
	}

	@Transactional
	void settle(Symbol symbol, List<Match> matches, Instant now) {
		for (Match match : matches) {
			settleMatch(symbol, match, now);
		}
	}

	private void settleMatch(Symbol symbol, Match match, Instant now) {
		Order buyOrder = findOrder(match.buyOrderId());
		Order sellOrder = findOrder(match.sellOrderId());

		buyOrder.fill(match.quantity(), now);
		sellOrder.fill(match.quantity(), now);

		walletService.transferLocked(match.buyerId(), match.sellerId(),
				OrderService.QUOTE_ASSET, match.quoteAmount(), now);

		walletService.transferLocked(match.sellerId(), match.buyerId(),
				symbol.baseAsset(), match.quantity(), now);

		BigDecimal priceImprovement = buyOrder.getPrice()
				.subtract(match.price())
				.multiply(match.quantity());
		if (priceImprovement.signum() > 0) {
			walletService.unlock(match.buyerId(), OrderService.QUOTE_ASSET, priceImprovement, now);
		}

		tradeRepository.save(Trade.of(symbol, match, now));
	}

	private Order findOrder(UUID orderId) {
		return orderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));
	}
}
