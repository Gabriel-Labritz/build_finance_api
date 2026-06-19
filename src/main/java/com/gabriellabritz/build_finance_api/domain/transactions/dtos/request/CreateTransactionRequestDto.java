package com.gabriellabritz.build_finance_api.domain.transactions.dtos.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gabriellabritz.build_finance_api.domain.transactions.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateTransactionRequestDto(
        @NotNull(message = "Informe o tipo da transação.")
        TransactionType type,

        @NotNull(message = "Informe o valor da transação.")
        @Positive(message = "Informe um valor positivo para transação.")
        BigDecimal amount,

        @NotNull(message = "Informe a categoria da transação.")
        UUID categoryId,

        @JsonFormat(pattern = "dd/MM/yyyy")
        @NotNull(message = "Informe a data da transação.")
        LocalDate date,

        @Length(max = 255, message = "O tamanho máximo da descrição é de 255 caracteres.")
        String description
) {
}
