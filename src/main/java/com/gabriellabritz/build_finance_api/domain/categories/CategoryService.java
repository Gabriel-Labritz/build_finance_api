package com.gabriellabritz.build_finance_api.domain.categories;

import com.gabriellabritz.build_finance_api.domain.categories.dtos.request.CreateCategoryRequestDto;
import com.gabriellabritz.build_finance_api.domain.categories.dtos.request.UpdateCategoryRequestDto;
import com.gabriellabritz.build_finance_api.domain.categories.dtos.response.CategoryResponseDto;
import com.gabriellabritz.build_finance_api.domain.categories.enums.CategoryType;
import com.gabriellabritz.build_finance_api.domain.user.User;
import com.gabriellabritz.build_finance_api.infra.exceptions.categories.CategoryAlreadyExistsException;
import com.gabriellabritz.build_finance_api.infra.exceptions.categories.CategoryNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryResponseDto create(CreateCategoryRequestDto createCategoryRequestDto, User userLogged) {
        String categoryName = createCategoryRequestDto.name().trim();

        if (categoryRepository.existsVisibleCategoryWithName(categoryName, userLogged.getId())) {
            throw new CategoryAlreadyExistsException("Já existe uma categoria com esse nome.");
        }

        Category category = categoryRepository.save(new Category(categoryName, createCategoryRequestDto.type(), userLogged));
        return new CategoryResponseDto(category);
    }

    public List<CategoryResponseDto> listAllCategories(User userLogged, CategoryType type) {
        return categoryRepository.findAllVisibleByUserId(userLogged.getId(), type).stream().map(
                CategoryResponseDto::new
        ).toList();
    }

    @Transactional
    public CategoryResponseDto updateCategory(UUID id, UpdateCategoryRequestDto updateCategoryRequestDto, User userLogged) {
        Category category = getCategory(id, userLogged.getId());

        Optional.ofNullable(updateCategoryRequestDto.name()).ifPresent(name -> {
            String newCategoryName = name.trim();

            if (categoryRepository.existsVisibleCategoryWithName(newCategoryName, userLogged.getId())) {
                throw new CategoryAlreadyExistsException("Já existe uma categoria com esse nome.");
            }

            category.rename(newCategoryName);
        });

        Optional.ofNullable(updateCategoryRequestDto.type()).ifPresent(category::changeType);
        return new CategoryResponseDto(category);
    }

    @Transactional
    public void deleteCategory(UUID id, User userLogged) {
        Category category = getCategory(id, userLogged.getId());
        categoryRepository.delete(category);
    }

    private Category getCategory(UUID id, UUID userId) {
        return categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new CategoryNotFoundException("A categoria não foi encontrada."));
    }
}
