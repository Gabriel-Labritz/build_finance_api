package com.gabriellabritz.build_finance_api.domain.recurring_transactions.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gabriellabritz.build_finance_api.domain.categories.dtos.response.CategoryResponseDto;
import com.gabriellabritz.build_finance_api.domain.recurring_transactions.RecurringTransaction;
import com.gabriellabritz.build_finance_api.domain.recurring_transactions.enums.Frequency;
import com.gabriellabritz.build_finance_api.domain.transactions.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RecurringTransactionResponseDto(
        UUID id,
        String title,
        String description,
        BigDecimal amount,
        TransactionType type,
        CategoryResponseDto category,
        Frequency frequency,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate startDate,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate endDate,
        Boolean active
) {
    public RecurringTransactionResponseDto(RecurringTransaction recurringTransaction) {
        this(
                recurringTransaction.getId(),
                recurringTransaction.getTitle(),
                recurringTransaction.getDescription(),
                recurringTransaction.getAmount(),
                recurringTransaction.getType(),
                new CategoryResponseDto(recurringTransaction.getCategory()),
                recurringTransaction.getFrequency(),
                recurringTransaction.getStartDate(),
                recurringTransaction.getEndDate(),
                recurringTransaction.isActive()
        );
    }
}
