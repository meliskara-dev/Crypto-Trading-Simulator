package com.meliskara.crypto_trading_simulator;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {

	@Autowired
	protected MockMvc mockMvc;

	protected String registerAndLogin() throws Exception {
		return registerAndLogin(uniqueEmail());
	}

	protected String registerAndLogin(String email) throws Exception {
		String body = """
				{"email": "%s", "password": "password123"}
				""".formatted(email);

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated());

		String response = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return JsonPath.read(response, "$.accessToken");
	}

	protected ResultActions placeOrder(String token, String idempotencyKey,
									   String side, String price, String quantity) throws Exception {
		String body = """
				{"symbol": "BTCUSDT", "side": "%s", "type": "LIMIT", "price": %s, "quantity": %s}
				""".formatted(side, price, quantity);

		return mockMvc.perform(post("/api/orders")
				.header("Authorization", "Bearer " + token)
				.header("Idempotency-Key", idempotencyKey)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body));
	}

	protected BigDecimal balance(String token, String asset, String field) throws Exception {
		String json = mockMvc.perform(get("/api/wallet/balances")
						.header("Authorization", "Bearer " + token))
				.andReturn().getResponse().getContentAsString();
		List<String> values = JsonPath.read(json, "$[?(@.asset == '" + asset + "')]." + field);
		return new BigDecimal(values.getFirst());
	}

	protected static String uniqueEmail() {
		return "user-" + UUID.randomUUID() + "@test.com";
	}
}