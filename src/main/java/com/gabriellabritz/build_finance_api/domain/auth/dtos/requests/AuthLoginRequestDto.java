package com.gabriellabritz.build_finance_api.domain.auth.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequestDto(
        @NotBlank(message = "Informe o seu email.")
        @Email(message = "O formato do email informado é inválido.")
        String email,

        @NotBlank(message = "Informe sua senha.")
        String password
) {
}
