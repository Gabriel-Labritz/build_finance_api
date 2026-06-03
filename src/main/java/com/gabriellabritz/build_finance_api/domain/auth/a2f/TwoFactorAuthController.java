package com.gabriellabritz.build_finance_api.domain.auth.a2f;

import com.gabriellabritz.build_finance_api.domain.auth.dtos.responses.TwoFactorSetupResponse;
import com.gabriellabritz.build_finance_api.domain.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/two-factor-auth")
public class TwoFactorAuthController {
    private final TwoFactorAuthService twoFactorAuthService;

    public TwoFactorAuthController(TwoFactorAuthService twoFactorAuthService) {
        this.twoFactorAuthService = twoFactorAuthService;
    }

    @PatchMapping("/config")
    public ResponseEntity<TwoFactorSetupResponse> genarateQRCode(@AuthenticationPrincipal User userLogged) {
        return ResponseEntity.ok().body(twoFactorAuthService.generateQRCode(userLogged));
    }
}
