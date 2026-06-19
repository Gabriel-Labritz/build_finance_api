package com.gabriellabritz.build_finance_api.domain.transactions.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gabriellabritz.build_finance_api.domain.categories.Category;
import com.gabriellabritz.build_finance_api.domain.transactions.Transaction;
import com.gabriellabritz.build_finance_api.domain.transactions.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionResponseDto(
        UUID id,
        TransactionType type,
        BigDecimal amount,
        Category category,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate date,
        String description
) {
    public TransactionResponseDto(Transaction transaction) {
        this(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getCategory(),
                transaction.getDate(),
                transaction.getDescription()
        );
    }
}
