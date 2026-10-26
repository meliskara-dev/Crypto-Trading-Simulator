package com.meliskara.crypto_trading_simulator;

import org.springframework.boot.SpringApplication;

public class TestCryptoTradingSimulatorApplication {

	public static void main(String[] args) {
		SpringApplication.from(CryptoTradingSimulatorApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
