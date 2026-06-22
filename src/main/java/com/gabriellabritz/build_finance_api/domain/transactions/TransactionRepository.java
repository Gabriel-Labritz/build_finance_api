package com.gabriellabritz.build_finance_api.domain.transactions;

import com.gabriellabritz.build_finance_api.domain.transactions.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.user.id = :userId
            AND (:type IS NULL OR t.type = :type)
            AND (:categoryName IS NULL OR t.category.name = :categoryName)
            AND (:amountGreaterThan IS NULL OR t.amount >= :amountGreaterThan)
            AND (:amountLessThan IS NULL OR t.amount <= :amountLessThan)
            AND (:startDate IS NULL OR t.date >= :startDate)
            AND (:endDate IS NULL OR t.date <= :endDate)
            """)
    List<Transaction> findAllWithFilters(
            @Param("userId") UUID userId,
            @Param("type") TransactionType type,
            @Param("categoryName") String categoryName,
            @Param("amountGreaterThan") BigDecimal amountGreaterThan,
            @Param("amountLessThan") BigDecimal amountLessThan,
            @Param("startDate")LocalDate startDate,
            @Param("endDate") LocalDate endDate
            );

    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.id = :id
            AND t.user.id = :userId
            """)
    Optional<Transaction> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);
}
