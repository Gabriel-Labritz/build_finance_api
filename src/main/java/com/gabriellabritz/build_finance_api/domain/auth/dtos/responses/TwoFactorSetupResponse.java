package com.gabriellabritz.build_finance_api.domain.auth.dtos.responses;

public record TwoFactorSetupResponse(
        String qrCodeUrl
) {
}
