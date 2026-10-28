package com.meliskara.crypto_trading_simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CryptoTradingSimulatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(CryptoTradingSimulatorApplication.class, args);
	}

}
