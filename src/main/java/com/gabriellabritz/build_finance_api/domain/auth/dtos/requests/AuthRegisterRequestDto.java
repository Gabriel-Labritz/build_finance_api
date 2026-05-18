package com.gabriellabritz.build_finance_api.domain.auth.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AuthRegisterRequestDto(
        @NotBlank(message = "Informe seu nome.")
        @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres.")
        String name,

        @NotBlank(message = "Informe seu email.")
        @Email(message = "O formato do email informado é inválido.")
        @Size(max = 100, message = "O email deve ter no máximo 100 caracteres.")
        String email,

        @NotBlank(message = "Informa sua senha.")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,}$",
                message = "A senha deve ter no mínimo 8 caracteres, contendo pelo menos, uma letra maiúscula, um número e um caractere especial."
        )
        String password
) {
}
