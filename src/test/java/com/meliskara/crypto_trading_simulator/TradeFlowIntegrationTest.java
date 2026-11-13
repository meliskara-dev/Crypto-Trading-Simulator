package com.meliskara.crypto_trading_simulator;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TradeFlowIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void matchSettlesBalancesAndCancelReleasesRemainder() throws Exception {
		String sellerEmail = uniqueEmail();
		String seller = registerAndLogin(sellerEmail);
		String buyer = registerAndLogin();
		giveBtc(sellerEmail, "1");

		// maker: SELL 0.3 @ 50000 deftere oturur
		String sellId = orderId(placeOrder(seller, UUID.randomUUID().toString(), "SELL", "50000", "0.3")
				.andExpect(status().isCreated()));
		// taker: BUY 0.1 @ 51000 -> maker fiyatından (50000) eşleşir
		String buyId = orderId(placeOrder(buyer, UUID.randomUUID().toString(), "BUY", "51000", "0.1")
				.andExpect(status().isCreated()));

		awaitStatus(buyer, buyId, "FILLED");
		awaitStatus(seller, sellId, "PARTIALLY_FILLED");

		// alıcı: 5100 kilitlendi, 5000 ödendi, 100 iade
		assertThat(balance(buyer, "USDT", "available")).isEqualByComparingTo("5000");
		assertThat(balance(buyer, "USDT", "locked")).isEqualByComparingTo("0");
		assertThat(balance(buyer, "BTC", "available")).isEqualByComparingTo("0.1");
		// satıcı: 0.3 kilitlendi, 0.1 satıldı
		assertThat(balance(seller, "USDT", "available")).isEqualByComparingTo("15000");
		assertThat(balance(seller, "BTC", "available")).isEqualByComparingTo("0.7");
		assertThat(balance(seller, "BTC", "locked")).isEqualByComparingTo("0.2");

		mockMvc.perform(delete("/api/orders/" + sellId).header("Authorization", "Bearer " + seller))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CANCELLED"));

		assertThat(balance(seller, "BTC", "available")).isEqualByComparingTo("0.9");
		assertThat(balance(seller, "BTC", "locked")).isEqualByComparingTo("0");
	}

	@Test
	void otherUsersOrderLooksLikeItDoesNotExist() throws Exception {
		String owner = registerAndLogin();
		String stranger = registerAndLogin();
		String id = orderId(placeOrder(owner, UUID.randomUUID().toString(), "BUY", "1000", "0.1"));

		mockMvc.perform(get("/api/orders/" + id).header("Authorization", "Bearer " + stranger))
				.andExpect(status().isNotFound());
		mockMvc.perform(delete("/api/orders/" + id).header("Authorization", "Bearer " + stranger))
				.andExpect(status().isNotFound());
	}

	private void giveBtc(String email, String amount) {
		jdbcTemplate.update("""
				UPDATE balances SET available = ?
				WHERE asset = 'BTC' AND user_id = (SELECT id FROM users WHERE email = ?)
				""", new BigDecimal(amount), email);
	}

	private void awaitStatus(String token, String orderId, String expected) {
		await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
				mockMvc.perform(get("/api/orders/" + orderId).header("Authorization", "Bearer " + token))
						.andExpect(jsonPath("$.status").value(expected)));
	}

	private static String orderId(ResultActions result) throws Exception {
		return JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id");
	}
}