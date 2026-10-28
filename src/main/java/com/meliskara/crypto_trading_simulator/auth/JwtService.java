package com.meliskara.crypto_trading_simulator.auth;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
class JwtService {

	private final JwtEncoder jwtEncoder;
	private final JwtProperties jwtProperties;

	JwtService(JwtEncoder jwtEncoder, JwtProperties props){
		this.jwtEncoder = jwtEncoder;
		this.jwtProperties = props;
	}

	String createAccessToken(UUID userId){
		Instant now = Instant.now();

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("crypto-trading-simulator")
				.subject(userId.toString())
				.issuedAt(now)
				.expiresAt(now.plus(jwtProperties.accessTokenTtl()))
				.build();

		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

		return jwtEncoder.encode(JwtEncoderParameters.from(header,claims))
				.getTokenValue();
	}
}
