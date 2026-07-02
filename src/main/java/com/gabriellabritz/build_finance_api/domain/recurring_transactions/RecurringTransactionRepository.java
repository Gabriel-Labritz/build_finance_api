package com.gabriellabritz.build_finance_api.domain.recurring_transactions;

import com.gabriellabritz.build_finance_api.domain.recurring_transactions.enums.Frequency;
import com.gabriellabritz.build_finance_api.domain.transactions.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, UUID> {
    @Query("""
            SELECT rt
            FROM RecurringTransaction rt
            WHERE rt.user.id = :userId
            AND (:type IS NULL OR rt.type = :type)
            AND (:category IS NULL OR rt.category.name = :category)
            AND (:frequency IS NULL OR rt.frequency = :frequency)
            AND (:active IS NULL OR rt.active = :active)
            """)
    List<RecurringTransaction> findAllRecurringTransaction(
            @Param("userId") UUID userId,
            @Param("type") TransactionType type,
            @Param("category") String category,
            @Param("frequency")Frequency frequency,
            @Param("active") Boolean active
            );

    Optional<RecurringTransaction> findByIdAndUserId(UUID id, UUID userId);

    List<RecurringTransaction> findByActiveTrueAndNextExecutionDateLessThanEqual(LocalDate today);
}
