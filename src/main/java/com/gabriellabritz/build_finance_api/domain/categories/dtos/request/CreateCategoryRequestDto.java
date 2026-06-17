package com.gabriellabritz.build_finance_api.domain.categories.dtos.request;

import com.gabriellabritz.build_finance_api.domain.categories.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequestDto(
        @NotBlank(message = "Informe o nome da categoria.")
        @Size(min = 3, max = 50, message = "O nome da categoria deve conter entre 3 a 50 caracteres.")
        String name,

        @NotNull(message = "Informe o tipo da categoria.")
        CategoryType type
) {
}
