package com.meliskara.crypto_trading_simulator.market;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/market")
@SecurityRequirements
public class MarketController {

	private final MarketService marketService;

	MarketController(MarketService marketService) {
		this.marketService = marketService;
	}

	@GetMapping("/{symbol}/price")
	PriceResponse getPrice(@PathVariable Symbol symbol) {
		return PriceResponse.of(symbol,
				marketService.getLastPrice(symbol));
	}

	@GetMapping("/{symbol}/candles")
	List<CandleResponse> getCandles(@PathVariable Symbol symbol,
									@RequestParam(defaultValue = "1m") Timeframe timeframe,
									@RequestParam(defaultValue = "100") @Min(1) @Max(500) int limit) {
		return marketService.getCandles(symbol, timeframe, limit);
	}

	@GetMapping("/{symbol}/stats")
	Stats24hResponse getStats(@PathVariable Symbol symbol) {
		return marketService.getStats24h(symbol, Instant.now());
	}
}
