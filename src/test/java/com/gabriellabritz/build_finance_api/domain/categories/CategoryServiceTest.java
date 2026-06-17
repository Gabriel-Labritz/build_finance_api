package com.gabriellabritz.build_finance_api.domain.categories;

import com.gabriellabritz.build_finance_api.domain.categories.dtos.request.CreateCategoryRequestDto;
import com.gabriellabritz.build_finance_api.domain.categories.dtos.request.UpdateCategoryRequestDto;
import com.gabriellabritz.build_finance_api.domain.categories.dtos.response.CategoryResponseDto;
import com.gabriellabritz.build_finance_api.domain.categories.enums.CategoryType;
import com.gabriellabritz.build_finance_api.domain.user.User;
import com.gabriellabritz.build_finance_api.infra.exceptions.categories.CategoryAlreadyExistsException;
import com.gabriellabritz.build_finance_api.infra.exceptions.categories.CategoryNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    @InjectMocks
    private CategoryService categoryService;

    private CreateCategoryRequestDto createCategoryRequestDto;

    @Mock
    private User user;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private Category category;

    @Captor
    private ArgumentCaptor<Category> categoryArgumentCaptor;

    UUID categoryId;

    @BeforeEach
    void setUp() {
        this.createCategoryRequestDto = new CreateCategoryRequestDto("New Category", CategoryType.EXPENSE);
        when(user.getId()).thenReturn(UUID.randomUUID());
        this.categoryId = UUID.randomUUID();
    }

    @Nested
    class create {
        @Test
        @DisplayName("Deve lançar a exceção CategoryAlreadyExistsException quando o nome da categoria já existe")
        void shouldThrowCategoryAlreadyExistsExceptionWhenCategoryNameAlreadyExists() {
            // Arrange
            when(categoryRepository.existsVisibleCategoryWithName(createCategoryRequestDto.name(), user.getId()))
                    .thenReturn(true);

            // Act + Assert
            assertThrows(CategoryAlreadyExistsException.class,
                    () -> categoryService.create(createCategoryRequestDto, user));
            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve salvar a categoria com sucesso")
        void shouldSaveCategorySuccessfully() {
            // Arrange
            when(categoryRepository.existsVisibleCategoryWithName(createCategoryRequestDto.name(), user.getId()))
                    .thenReturn(false);
            when(categoryRepository.save(any())).thenReturn(category);

            // Act
            categoryService.create(createCategoryRequestDto, user);

            // Assert
            verify(categoryRepository).save(categoryArgumentCaptor.capture());

            Category categoryCaptured = categoryArgumentCaptor.getValue();
            assertEquals(createCategoryRequestDto.name().trim(), categoryCaptured.getName());
            assertEquals(createCategoryRequestDto.type(), categoryCaptured.getType());
            assertEquals(user.getId(), categoryCaptured.getUser().getId());
        }
    }

    @Nested
    class listAllCategories {
        @Test
        @DisplayName("Deve listar todas as categorias sem filtro")
        void shouldListAllCategoriesWithoutFilter() {
            // Arrange
            when(categoryRepository.findAllVisibleByUserId(user.getId(), null)).thenReturn(List.of(category));

            // Act
            List<CategoryResponseDto> result = categoryService.listAllCategories(user, null);

            // Assert
            verify(categoryRepository)
                    .findAllVisibleByUserId(user.getId(), null);
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Deve listar todas as categorias com o filtro aplicado")
        void shouldListAllCategoriesWithFilterApplied() {
            // Arrange
            when(categoryRepository.findAllVisibleByUserId(user.getId(), CategoryType.EXPENSE)).thenReturn(List.of(category));

            // Act
            List<CategoryResponseDto> result = categoryService.listAllCategories(user, CategoryType.EXPENSE);

            // Assert
            verify(categoryRepository)
                    .findAllVisibleByUserId(user.getId(), CategoryType.EXPENSE);
            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    @Nested
    class updateCategory {
        @Test
        @DisplayName("Deve lançar a exceção CategoryNotFoundException quando a categoria não for encontrada, ou o usuário tentar atualizar uma categoria padrão ou que não pertence a ele")
        void shouldThrowCategoryNotFoundException() {
            // Arrange
            UpdateCategoryRequestDto updateCategoryRequestDto =
                    new UpdateCategoryRequestDto("UpdatedName", CategoryType.EXPENSE);
            when(categoryRepository.findByIdAndUserId(categoryId, user.getId())).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(CategoryNotFoundException.class,
                    () -> categoryService.updateCategory(categoryId, updateCategoryRequestDto, user));
            verify(category, never()).rename(any());
            verify(category, never()).changeType(any());
        }

        @Test
        @DisplayName("Deve lançar a exceção CategoryAlreadyExistsException quando o nome enviado para atualização já existe")
        void shouldThrowCategoryAlreadyExistsExceptionWhenCategoryNameAlreadyExists() {
            // Arrange
            UpdateCategoryRequestDto updateCategoryRequestDto =
                    new UpdateCategoryRequestDto("UpdatedName", CategoryType.EXPENSE);
            when(categoryRepository.findByIdAndUserId(categoryId, user.getId())).thenReturn(Optional.of(category));
            when(categoryRepository.existsVisibleCategoryWithName(updateCategoryRequestDto.name(), user.getId()))
                    .thenReturn(true);

            // Act + Assert
            assertThrows(CategoryAlreadyExistsException.class,
                    () -> categoryService.updateCategory(categoryId, updateCategoryRequestDto, user));
            verify(category, never()).rename(any());
            verify(category, never()).changeType(any());
        }

        @Test
        @DisplayName("Deve atualizar a categoria com somente o nome enviado")
        void shouldUpdateCategoryWhenCategoryNameIsSend() {
            // Arrange
            UpdateCategoryRequestDto updateCategoryRequestDto =
                    new UpdateCategoryRequestDto("UpdatedName", null);

            when(categoryRepository.findByIdAndUserId(categoryId, user.getId())).thenReturn(Optional.of(category));
            when(categoryRepository.existsVisibleCategoryWithName(updateCategoryRequestDto.name(), user.getId()))
                    .thenReturn(false);

            // Act
            categoryService.updateCategory(categoryId, updateCategoryRequestDto, user);

            // Assert
            verify(category).rename(updateCategoryRequestDto.name().trim());
            verify(category, never()).changeType(any());
        }

        @Test
        @DisplayName("Deve atualizar a categoria com somente o tipo da categoria enviada")
        void shouldUpdateCategoryWhenCategoryTypeIsSend() {
            // Arrange
            UpdateCategoryRequestDto updateCategoryRequestDto =
                    new UpdateCategoryRequestDto(null, CategoryType.EXPENSE);
            when(categoryRepository.findByIdAndUserId(categoryId, user.getId())).thenReturn(Optional.of(category));

            // Act
            categoryService.updateCategory(categoryId, updateCategoryRequestDto, user);

            // Assert
            verify(category).changeType(CategoryType.EXPENSE);
            verify(category, never()).rename(any());
        }

        @Test
        @DisplayName("Deve atualizar a categoria com o nome e tipo enviado")
        void shouldUpdateCategoryWhenCategoryNameAndTypeIsSend() {
            // Arrange
            UpdateCategoryRequestDto updateCategoryRequestDto =
                    new UpdateCategoryRequestDto("UpdateCategory", CategoryType.EXPENSE);
            when(categoryRepository.findByIdAndUserId(categoryId, user.getId())).thenReturn(Optional.of(category));
            when(categoryRepository.existsVisibleCategoryWithName("UpdateCategory", user.getId()))
                    .thenReturn(false);

            // Act
            categoryService.updateCategory(categoryId, updateCategoryRequestDto, user);

            // Assert
            verify(category).rename(updateCategoryRequestDto.name().trim());
            verify(category).changeType(CategoryType.EXPENSE);
        }
    }

    @Nested
    class deleteCategory {
        @Test
        @DisplayName("Deve lançar a exceção CategoryNotFoundException quando a categoria não for encontrada, ou o usuário tentar deletar uma categoria padrão ou que não pertence a ele")
        void shouldThrowCategoryNotFoundException() {
            // Arrange
            when(categoryRepository.findByIdAndUserId(categoryId, user.getId())).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(CategoryNotFoundException.class,
                    () -> categoryService.deleteCategory(categoryId, user));
            verify(categoryRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Deve deletar a categoria com sucesso")
        void shouldDeleteCategoryWithSuccessfully() {
            // Arrange
            when(categoryRepository.findByIdAndUserId(categoryId, user.getId())).thenReturn(Optional.of(category));

            // Act + Assert
            categoryService.deleteCategory(categoryId, user);

            verify(categoryRepository).delete(category);
        }
    }
}