package com.gabriellabritz.build_finance_api.domain.recurring_transactions;

import com.gabriellabritz.build_finance_api.domain.categories.Category;
import com.gabriellabritz.build_finance_api.domain.recurring_transactions.enums.Frequency;
import com.gabriellabritz.build_finance_api.domain.transactions.enums.TransactionType;
import com.gabriellabritz.build_finance_api.domain.user.User;
import com.gabriellabritz.build_finance_api.infra.exceptions.recurring_transactions.InvalidRecurringTransactionDateException;
import com.gabriellabritz.build_finance_api.infra.exceptions.recurring_transactions.RecurringTransactionAlreadyActiveException;
import com.gabriellabritz.build_finance_api.infra.exceptions.recurring_transactions.RecurringTransactionAlreadyInactiveException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "recurring_transactions")
public class RecurringTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Frequency frequency;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private LocalDate nextExecutionDate;

    private LocalDate lastExecutionDate;

    @Column(nullable = false)
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public RecurringTransaction(
            String title,
            String description,
            BigDecimal amount,
            TransactionType type,
            Category category,
            Frequency frequency,
            LocalDate startDate,
            LocalDate endDate,
            User user
    ) {
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.frequency = frequency;
        this.startDate = startDate;
        this.nextExecutionDate = startDate;
        this.endDate = endDate;
        this.user = user;
        this.active = true;
    }

    public static void validateStartDate(LocalDate startDate) {
        if (startDate.isBefore(LocalDate.now())) {
            throw new InvalidRecurringTransactionDateException("A data de início informada é anterior a data atual.");
        }
    }

    public static void validateEndDate(LocalDate startDate, LocalDate endDate) {
        if (!endDate.isAfter(startDate)) {
            throw new InvalidRecurringTransactionDateException("A data final informada não pode ser anterior ou igual a data de início.");
        }
    }

    public void disable() {
        if (!this.active) {
            throw new RecurringTransactionAlreadyInactiveException("A transação recorrente já está desativada.");
        }
        this.active = false;
    }

    public void activate() {
        if (this.active) {
            throw new RecurringTransactionAlreadyActiveException("A transação recorrente já está ativa.");
        }
        this.active = true;
    }

    public void update(
            String title,
            String description,
            BigDecimal amount,
            TransactionType type,
            Category category,
            Frequency frequency,
            LocalDate endDate
    ) {
        Optional.ofNullable(title).ifPresent(value -> this.title = value);
        Optional.ofNullable(description).ifPresent(value -> this.description = value);
        Optional.ofNullable(amount).ifPresent(value -> this.amount = value);
        Optional.ofNullable(type).ifPresent(value -> this.type = value);
        Optional.ofNullable(category).ifPresent(value -> this.category = value);
        Optional.ofNullable(frequency).ifPresent(value -> this.frequency = value);
        Optional.ofNullable(endDate).ifPresent(value -> this.endDate = value);
    }

    public void advanceNextDueDate() {
        this.lastExecutionDate = this.nextExecutionDate;
        this.nextExecutionDate = switch (this.frequency) {
            case DAILY -> this.nextExecutionDate.plusDays(1);
            case WEEKLY -> this.nextExecutionDate.plusWeeks(1);
            case MONTHLY -> this.nextExecutionDate.plusMonths(1);
            case YEARLY -> this.nextExecutionDate.plusYears(1);
        };
    }
}
