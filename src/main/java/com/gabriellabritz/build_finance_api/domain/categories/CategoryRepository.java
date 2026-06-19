package com.gabriellabritz.build_finance_api.domain.categories;

import com.gabriellabritz.build_finance_api.domain.categories.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    @Query("""
            SELECT COUNT(c) > 0
            FROM Category c
            WHERE LOWER(c.name) = LOWER(:name)
            AND (
                c.defaultCategory = true
                OR c.user.id = :userId
            )
            """)
    Boolean existsVisibleCategoryWithName(@Param("name") String name, @Param("userId") UUID userId);

    @Query("""
        SELECT c
        FROM Category c
        WHERE (c.defaultCategory = true OR c.user.id = :userId)
        AND (:type IS NULL OR c.type = :type)
        ORDER BY c.name
        """)
    List<Category> findAllVisibleByUserId(@Param("userId") UUID userId, @Param("type")CategoryType type);

    @Query("""
            SELECT c
            FROM Category c
            WHERE c.id = :id
            AND c.defaultCategory = false
            AND c.user.id = :userId
            """)
    Optional<Category> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query("""
            SELECT c
            FROM Category c
            WHERE c.id = :id
            AND (c.user.id = :userId OR c.defaultCategory = true)
            """)
    Optional<Category> findCategoryByIdAndUser(@Param("id") UUID id, @Param("userId") UUID userId);
}
