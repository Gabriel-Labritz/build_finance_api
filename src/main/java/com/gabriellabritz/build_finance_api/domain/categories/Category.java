package com.gabriellabritz.build_finance_api.domain.categories;

import com.gabriellabritz.build_finance_api.domain.categories.enums.CategoryType;
import com.gabriellabritz.build_finance_api.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private Boolean defaultCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Category(String name, CategoryType categoryType, User user) {
        this.name = name;
        this.type = categoryType;
        this.user = user;
        this.defaultCategory = false;
    }
}
