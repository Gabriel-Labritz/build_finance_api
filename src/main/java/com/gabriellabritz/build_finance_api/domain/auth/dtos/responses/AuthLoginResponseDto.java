package com.gabriellabritz.build_finance_api.domain.auth.dtos.responses;

public record AuthLoginResponseDto(
        Boolean requiresTwoFactor,
        String accessToken,
        String refreshToken,
        String preAuthToken
) {
}
