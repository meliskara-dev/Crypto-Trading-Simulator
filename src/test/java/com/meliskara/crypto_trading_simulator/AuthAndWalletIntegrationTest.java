package com.meliskara.crypto_trading_simulator;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthAndWalletIntegrationTest extends AbstractIntegrationTest {

	@Test
	void newUserGetsDemoBalance() throws Exception {
		String token = registerAndLogin();

		mockMvc.perform(get("/api/wallet/balances")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.asset == 'USDT')].available").value(contains("10000")))
				.andExpect(jsonPath("$[?(@.asset == 'BTC')].available").value(contains("0")));
	}

	@Test
	void registeringSameEmailTwiceReturns409() throws Exception {
		String body = """
				{"email": "%s", "password": "password123"}
				""".formatted(uniqueEmail());

		mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	@Test
	void invalidEmailReturns400WithFieldError() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email": "not-an-email", "password": "password123"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.email").exists());
	}

	@Test
	void balancesWithoutTokenReturns401ProblemDetail() throws Exception {
		mockMvc.perform(get("/api/wallet/balances"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.title").value("Authentication failed"));
	}

	@Test
	void placingBuyOrderLocksUsdt() throws Exception {
		String token = registerAndLogin();

		placeOrder(token, UUID.randomUUID().toString(), "BUY", "1000", "0.1")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("NEW"));

		mockMvc.perform(get("/api/wallet/balances")
						.header("Authorization", "Bearer " + token))
				.andExpect(jsonPath("$[?(@.asset == 'USDT')].available").value(contains("9900")))
				.andExpect(jsonPath("$[?(@.asset == 'USDT')].locked").value(contains("100")));
	}
}