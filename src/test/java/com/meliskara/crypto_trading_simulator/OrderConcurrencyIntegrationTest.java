package com.meliskara.crypto_trading_simulator;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class OrderConcurrencyIntegrationTest extends AbstractIntegrationTest {

	private static final int THREADS = 32;

	@Test
	void hundredParallelOrdersNeverOverdrawBalance() throws Exception {
		String token = registerAndLogin();

		// her emir 1000 × 0.15 = 150 USDT; 10000 USDT en fazla 66 emre yeter
		List<Integer> statuses = runInParallel(100,
				() -> placeOrder(token, UUID.randomUUID().toString(), "BUY", "1000", "0.15"));

		int created = (int) statuses.stream().filter(s -> s == 201).count();
		BigDecimal available = usdt(token, "available");
		BigDecimal locked = usdt(token, "locked");

		assertThat(statuses).allMatch(s -> s == 201 || s == 409 || s == 422);
		assertThat(created).isBetween(1, 66);
		assertThat(available).isGreaterThanOrEqualTo(BigDecimal.ZERO);
		assertThat(available.add(locked)).isEqualByComparingTo("10000");
		assertThat(locked).isEqualByComparingTo(new BigDecimal("150").multiply(BigDecimal.valueOf(created)));
		assertThat(orderCount(token)).isEqualTo(created);
	}

	@Test
	void tenParallelRequestsWithSameKeyCreateOneOrder() throws Exception {
		String token = registerAndLogin();
		String key = UUID.randomUUID().toString();

		List<Integer> statuses = runInParallel(10,
				() -> placeOrder(token, key, "BUY", "1000", "0.15"));

		assertThat(statuses).contains(201).allMatch(s -> s == 201 || s == 409);
		assertThat(orderCount(token)).isEqualTo(1);
		assertThat(usdt(token, "locked")).isEqualByComparingTo("150");
	}

	@FunctionalInterface
	private interface Request {
		ResultActions send() throws Exception;
	}

	private List<Integer> runInParallel(int count, Request request) throws Exception {
		CountDownLatch startGate = new CountDownLatch(1);
		List<Future<Integer>> futures = new ArrayList<>();

		try (ExecutorService pool = Executors.newFixedThreadPool(THREADS)) {
			for (int i = 0; i < count; i++) {
				futures.add(pool.submit(() -> {
					startGate.await();
					return request.send().andReturn().getResponse().getStatus();
				}));
			}
			startGate.countDown();
		}

		List<Integer> statuses = new ArrayList<>();
		for (Future<Integer> future : futures) {
			statuses.add(future.get());
		}
		return statuses;
	}

	private BigDecimal usdt(String token, String field) throws Exception {
		String json = mockMvc.perform(get("/api/wallet/balances")
						.header("Authorization", "Bearer " + token))
				.andReturn().getResponse().getContentAsString();
		List<String> values = JsonPath.read(json, "$[?(@.asset == 'USDT')]." + field);
		return new BigDecimal(values.getFirst());
	}

	private int orderCount(String token) throws Exception {
		String json = mockMvc.perform(get("/api/orders?limit=200")
						.header("Authorization", "Bearer " + token))
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(json, "$.length()");
	}
}