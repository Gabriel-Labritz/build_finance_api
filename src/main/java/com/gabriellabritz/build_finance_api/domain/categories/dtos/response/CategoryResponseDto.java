package com.gabriellabritz.build_finance_api.domain.categories.dtos.response;

import com.gabriellabritz.build_finance_api.domain.categories.Category;
import com.gabriellabritz.build_finance_api.domain.categories.enums.CategoryType;

import java.util.UUID;

public record CategoryResponseDto(
        UUID id,
        String name,
        CategoryType type
) {
    public CategoryResponseDto(Category category) {
        this(category.getId(), category.getName(), category.getType());
    }
}
