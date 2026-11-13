package com.meliskara.crypto_trading_simulator;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MarketIntegrationTest extends AbstractIntegrationTest {

	private static final Duration TIMEOUT = Duration.ofSeconds(20);

	@Test
	void lastPriceArrivesThroughKafkaAndRedis() {
		await().atMost(TIMEOUT).untilAsserted(() ->
				mockMvc.perform(get("/api/market/BTCUSDT/price"))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.symbol").value("BTCUSDT"))
						.andExpect(jsonPath("$.price").value(not(emptyString()))));
	}

	@Test
	void candlesAndStatsAreBuiltFromTicks() {
		await().atMost(TIMEOUT).untilAsserted(() ->
				mockMvc.perform(get("/api/market/ETHUSDT/candles?timeframe=1m&limit=10"))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$[0].open").exists())
						.andExpect(jsonPath("$[0].openTime").exists()));

		await().atMost(TIMEOUT).untilAsserted(() ->
				mockMvc.perform(get("/api/market/ETHUSDT/stats"))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.high").exists())
						.andExpect(jsonPath("$.changePercent").exists()));
	}

	@Test
	void invalidParametersReturn400() throws Exception {
		mockMvc.perform(get("/api/market/DOGEUSDT/price"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(get("/api/market/BTCUSDT/candles?timeframe=2m"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(get("/api/market/BTCUSDT/candles?limit=1000"))
				.andExpect(status().isBadRequest());
	}
}