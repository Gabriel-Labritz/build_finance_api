package com.gabriellabritz.build_finance_api.domain.auth.account_verification;

import com.gabriellabritz.build_finance_api.domain.auth.dtos.responses.VerifiedUserResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
public class AccountVerificationController {
    private final AccountVerificationService accountVerificationService;

    public AccountVerificationController(AccountVerificationService accountVerificationService) {
        this.accountVerificationService = accountVerificationService;
    }

    @GetMapping("/verify-account")
    public ResponseEntity<VerifiedUserResponseDto> verifyAccount(@RequestParam String token) {
        return ResponseEntity.ok().body(accountVerificationService.verifyAccount(token));
    }
}
