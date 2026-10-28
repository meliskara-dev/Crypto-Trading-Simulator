package com.meliskara.crypto_trading_simulator.auth;

import com.meliskara.crypto_trading_simulator.wallet.WalletService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final WalletService walletService;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;
	private final JwtProperties jwtProperties;

	public AuthService(UserRepository userRepository,
					   PasswordEncoder passwordEncoder,
					   WalletService walletService,
					   JwtService jwtService,
					   RefreshTokenService refreshTokenService,
					   JwtProperties jwtProperties) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.walletService = walletService;
		this.jwtService = jwtService;
		this.refreshTokenService = refreshTokenService;
		this.jwtProperties = jwtProperties;
	}

	@Transactional
	public RegisterResponse register(RegisterRequest request){
		String email = normalizeEmail(request.email());

		if (userRepository.existsByEmail(email)) {
			throw new EmailAlreadyTakenException(email);
		}

		String hash = passwordEncoder.encode(request.password());
		User user = userRepository.save(new User(email, hash));
		walletService.createInitialBalances(user.getId());

		return new RegisterResponse(user.getId(), user.getEmail());
	}

	@Transactional
	public TokenResponse login(LoginRequest request){
		String email = normalizeEmail(request.email());

		User user = userRepository.findByEmail(email)
				.filter(u -> passwordEncoder.matches(request.password(),
						u.getPasswordHash()))
				.orElseThrow(InvalidCredentialsException::new);

		Instant now = Instant.now();
		String accessToken = jwtService.createAccessToken(user.getId());
		String refreshToken = refreshTokenService.create(user.getId(), now);

		return new TokenResponse(
				accessToken,
				refreshToken,
				"Bearer",
				jwtProperties.accessTokenTtl().toSeconds());
	}

	private static String normalizeEmail(String email){
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
