package com.gabriellabritz.build_finance_api.domain.auth.a2f.dtos.response;

public record TwoFactorSetupResponse(
        String qrCodeUrl
) {
}
