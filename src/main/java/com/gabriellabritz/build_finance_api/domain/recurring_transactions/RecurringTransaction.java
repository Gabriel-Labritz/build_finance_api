package com.gabriellabritz.build_finance_api.domain.recurring_transactions;

import com.gabriellabritz.build_finance_api.domain.categories.Category;
import com.gabriellabritz.build_finance_api.domain.recurring_transactions.enums.Frequency;
import com.gabriellabritz.build_finance_api.domain.transactions.enums.TransactionType;
import com.gabriellabritz.build_finance_api.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
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
}
