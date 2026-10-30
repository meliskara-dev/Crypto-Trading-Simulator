package com.meliskara.crypto_trading_simulator.wallet;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

	private final WalletService walletService;

	WalletController(WalletService walletService) {
		this.walletService = walletService;
	}

	@GetMapping("/balances")
	List<BalanceResponse> getBalances(@AuthenticationPrincipal Jwt jwt){
		UUID userId = UUID.fromString(jwt.getSubject());
		return walletService.getBalances(userId);
	}
}
