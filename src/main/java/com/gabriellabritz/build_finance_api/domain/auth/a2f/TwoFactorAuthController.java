package com.gabriellabritz.build_finance_api.domain.auth.a2f;

import com.gabriellabritz.build_finance_api.domain.auth.dtos.requests.TwoFactorAuthRequestDto;
import com.gabriellabritz.build_finance_api.domain.auth.dtos.responses.AuthLoginResponseDto;
import com.gabriellabritz.build_finance_api.domain.auth.dtos.responses.TwoFactorSetupResponse;
import com.gabriellabritz.build_finance_api.domain.user.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/two-factor-auth")
public class TwoFactorAuthController {
    private final TwoFactorAuthService twoFactorAuthService;

    public TwoFactorAuthController(TwoFactorAuthService twoFactorAuthService) {
        this.twoFactorAuthService = twoFactorAuthService;
    }

    @PatchMapping("/setup")
    public ResponseEntity<TwoFactorSetupResponse> genarateQRCode(@AuthenticationPrincipal User userLogged) {
        return ResponseEntity.ok().body(twoFactorAuthService.generateQRCode(userLogged));
    }

    @PostMapping("/enable")
    public ResponseEntity<Void> enableTwoFactorAuth(
            @AuthenticationPrincipal User userLogged,
            @RequestBody @Valid TwoFactorAuthRequestDto twoFactorAuthRequestDto) {
        twoFactorAuthService.enableTwoFactorAuth(userLogged, twoFactorAuthRequestDto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/disable")
    public ResponseEntity<Void> disableTwoFactorAuth(
            @AuthenticationPrincipal User userLogged,
            @RequestBody @Valid TwoFactorAuthRequestDto twoFactorAuthRequestDto
    ) {
        twoFactorAuthService.disableTwoFactorAuth(userLogged, twoFactorAuthRequestDto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify")
    public ResponseEntity<AuthLoginResponseDto> verifyTwoFactorAuth(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody @Valid TwoFactorAuthRequestDto twoFactorAuthRequestDto
    ) {
        return ResponseEntity.ok().body(twoFactorAuthService.verifyTwoFactorAuth(authHeader, twoFactorAuthRequestDto));
    }
}
