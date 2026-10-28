package com.meliskara.crypto_trading_simulator.auth;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
class RefreshTokenService {

	private static final SecureRandom RANDOM = new SecureRandom();

	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtProperties jwtProperties;

	RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtProperties jwtProperties){
		this.refreshTokenRepository = refreshTokenRepository;
		this.jwtProperties = jwtProperties;
	}

	String create(UUID userId, Instant now){
		byte[] bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

		Instant expiresAt = now.plus(jwtProperties.refreshTokenTtl());
		refreshTokenRepository.save(new RefreshToken(userId, hash(token), expiresAt));

		return token;
	}

	static String hash(String token){
		try{
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hashed);
		} catch (NoSuchAlgorithmException e){
			throw new IllegalStateException("SHA-256 algorithm not available", e);
		}
	}
}
