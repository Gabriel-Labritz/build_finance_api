package com.gabriellabritz.build_finance_api.domain.auth.dtos.requests;

import jakarta.validation.constraints.NotBlank;

public record TwoFactorAuthRequestDto(
        @NotBlank(message = "Informe o código gerado pelo APP autenticador.")
        String code
) {
}
