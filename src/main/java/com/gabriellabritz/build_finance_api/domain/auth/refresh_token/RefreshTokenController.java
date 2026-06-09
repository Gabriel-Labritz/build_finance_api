package com.gabriellabritz.build_finance_api.domain.auth.refresh_token;

import com.gabriellabritz.build_finance_api.domain.auth.refresh_token.dtos.RefreshTokenRequestDto;
import com.gabriellabritz.build_finance_api.domain.auth.dtos.responses.AuthLoginResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/refresh-token")
public class RefreshTokenController {
    private final RefreshTokenService refreshTokenService;

    public RefreshTokenController(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping
    public ResponseEntity<AuthLoginResponseDto> refreshToken(@RequestBody @Valid RefreshTokenRequestDto refreshTokenRequestDto) {
        return ResponseEntity.ok().body(refreshTokenService.generateNewRefreshToken(refreshTokenRequestDto));
    }
}
