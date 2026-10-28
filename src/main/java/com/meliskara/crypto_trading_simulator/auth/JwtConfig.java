package com.meliskara.crypto_trading_simulator.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
class JwtConfig {

	private final SecretKey key;

	JwtConfig(JwtProperties props){
		this.key = new SecretKeySpec(
				props.secret().getBytes(StandardCharsets.UTF_8),
				"HmacSHA256");
	}

	@Bean
	JwtEncoder jwtEncoder(){
		return NimbusJwtEncoder.withSecretKey(key).build();
	}

	@Bean
	JwtDecoder jwtDecoder(){
		return NimbusJwtDecoder.withSecretKey(key)
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
	}
}
