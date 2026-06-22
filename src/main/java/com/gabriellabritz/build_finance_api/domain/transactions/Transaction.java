package com.gabriellabritz.build_finance_api.domain.transactions;

import com.gabriellabritz.build_finance_api.domain.categories.Category;
import com.gabriellabritz.build_finance_api.domain.transactions.enums.TransactionType;
import com.gabriellabritz.build_finance_api.domain.user.User;
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
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    private String description;

    public Transaction(
            TransactionType type,
            BigDecimal amount,
            Category category,
            User user,
            LocalDate date,
            String description
    ) {
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.user = user;
        this.date = date;
        this.description = description;
    }

    public void update(TransactionType type, BigDecimal amount, Category category, LocalDate date, String description) {
        Optional.ofNullable(type).ifPresent(value -> this.type = value);
        Optional.ofNullable(amount).ifPresent(value -> this.amount = value);
        Optional.ofNullable(category).ifPresent(value -> this.category = value);
        Optional.ofNullable(date).ifPresent(value -> this.date = value);
        Optional.ofNullable(description).ifPresent(value -> this.description = value);
    }
}

