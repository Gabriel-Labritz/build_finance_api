package com.gabriellabritz.build_finance_api.domain.transactions.dtos.response;

import com.gabriellabritz.build_finance_api.domain.transactions.Transaction;
import com.gabriellabritz.build_finance_api.domain.transactions.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionDetailsResponseDto(
        UUID id,
        TransactionType type,
        BigDecimal amount,
        String category,
        LocalDate date,
        String description
) {
    public TransactionDetailsResponseDto(Transaction transaction) {
        this(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getCategory().getName(),
                transaction.getDate(),
                transaction.getDescription()
        );
    }
}
