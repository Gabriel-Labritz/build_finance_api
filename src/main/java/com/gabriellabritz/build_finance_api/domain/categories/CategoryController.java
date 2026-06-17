package com.gabriellabritz.build_finance_api.domain.categories;

import com.gabriellabritz.build_finance_api.domain.categories.dtos.request.CreateCategoryRequestDto;
import com.gabriellabritz.build_finance_api.domain.categories.dtos.request.UpdateCategoryRequestDto;
import com.gabriellabritz.build_finance_api.domain.categories.dtos.response.CategoryResponseDto;
import com.gabriellabritz.build_finance_api.domain.categories.enums.CategoryType;
import com.gabriellabritz.build_finance_api.domain.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/category")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDto> createCategory(
            @RequestBody @Valid CreateCategoryRequestDto createCategoryRequestDto,
            @AuthenticationPrincipal User userLogged
            ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(createCategoryRequestDto, userLogged));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> listAllCategories(
            @AuthenticationPrincipal User userLogged,
            @RequestParam(required = false) CategoryType type) {
        return ResponseEntity.ok().body(categoryService.listAllCategories(userLogged, type));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> editCategory(
            @PathVariable UUID id,
            @RequestBody UpdateCategoryRequestDto updateCategoryRequestDto,
            @AuthenticationPrincipal User userLogged) {
        return ResponseEntity.ok().body(categoryService.updateCategory(id, updateCategoryRequestDto, userLogged));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id, @AuthenticationPrincipal User userLogged) {
        categoryService.deleteCategory(id, userLogged);
        return ResponseEntity.noContent().build();
    }
}
