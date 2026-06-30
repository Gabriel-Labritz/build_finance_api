package com.gabriellabritz.build_finance_api.domain.recurring_transactions.dtos.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gabriellabritz.build_finance_api.domain.recurring_transactions.enums.Frequency;
import com.gabriellabritz.build_finance_api.domain.transactions.enums.TransactionType;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateRecurringTransactionRequestDto(
        @Size(min = 3, max = 50, message = "O título da transação deve conter entre 3 a 50 caracteres.")
        String title,

        @Size(max = 255, message = "A descrição deve conter no máximo 255 caracteres.")
        String description,

        @Positive(message = "Informe um valor positivo para transação.")
        BigDecimal amount,

        TransactionType type,
        
        UUID categoryId,

        Frequency frequency,

        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate endDate
) {
}
