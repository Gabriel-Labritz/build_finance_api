package com.gabriellabritz.build_finance_api.domain.transactions.dtos.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gabriellabritz.build_finance_api.domain.categories.Category;
import com.gabriellabritz.build_finance_api.domain.transactions.enums.TransactionType;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateTransactionRequestDto(
        TransactionType type,
        @Positive(message = "Informe um valor positivo para transação.")
        BigDecimal amount,
        UUID categoryId,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate date,
        String description
) {
}
