package com.meliskara.crypto_trading_simulator.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder){
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public RegisterResponse register(RegisterRequest request){
		String email = request.email().trim().toLowerCase(Locale.ROOT);

		if (userRepository.existsByEmail(email)) {
			throw new EmailAlreadyTakenException(email);
		}

		String hash = passwordEncoder.encode(request.password());
		User user = userRepository.save(new User(email, hash));

		return new RegisterResponse(user.getId(), user.getEmail());
	}
}
