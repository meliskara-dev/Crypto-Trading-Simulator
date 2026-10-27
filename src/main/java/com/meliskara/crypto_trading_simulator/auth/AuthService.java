package com.meliskara.crypto_trading_simulator.auth;

import com.meliskara.crypto_trading_simulator.wallet.WalletService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final WalletService walletService;

	public AuthService(UserRepository userRepository,
					   PasswordEncoder passwordEncoder,
					   WalletService walletService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.walletService = walletService;
	}

	@Transactional
	public RegisterResponse register(RegisterRequest request){
		String email = request.email().trim().toLowerCase(Locale.ROOT);

		if (userRepository.existsByEmail(email)) {
			throw new EmailAlreadyTakenException(email);
		}

		String hash = passwordEncoder.encode(request.password());
		User user = userRepository.save(new User(email, hash));
		walletService.createInitialBalances(user.getId());

		return new RegisterResponse(user.getId(), user.getEmail());
	}
}
