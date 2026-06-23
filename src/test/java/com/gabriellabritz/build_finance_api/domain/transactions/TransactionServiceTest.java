package com.gabriellabritz.build_finance_api.domain.transactions;

import com.gabriellabritz.build_finance_api.domain.categories.Category;
import com.gabriellabritz.build_finance_api.domain.categories.CategoryRepository;
import com.gabriellabritz.build_finance_api.domain.categories.enums.CategoryType;
import com.gabriellabritz.build_finance_api.domain.transactions.dtos.request.CreateTransactionRequestDto;
import com.gabriellabritz.build_finance_api.domain.transactions.dtos.request.UpdateTransactionRequestDto;
import com.gabriellabritz.build_finance_api.domain.transactions.dtos.response.ListTransactionsResponseDto;
import com.gabriellabritz.build_finance_api.domain.transactions.dtos.response.TransactionResponseDto;
import com.gabriellabritz.build_finance_api.domain.transactions.enums.TransactionType;
import com.gabriellabritz.build_finance_api.domain.user.User;
import com.gabriellabritz.build_finance_api.infra.exceptions.categories.CategoryNotFoundException;
import com.gabriellabritz.build_finance_api.infra.exceptions.transactions.TransactionNotFoundException;
import com.gabriellabritz.build_finance_api.infra.exceptions.transactions.TransactionTypeMismatchException;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private User user;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private Category category;

    @Mock
    private Transaction transaction;

    private CreateTransactionRequestDto createTransactionRequestDto;

    @Captor
    private ArgumentCaptor<Transaction> transactionArgumentCaptor;

    @Mock
    private Category currentCategory;

    @Mock
    private Category newCategory;

    @BeforeEach
    void setUp() {
        this.createTransactionRequestDto = new CreateTransactionRequestDto(TransactionType.EXPENSE, BigDecimal.valueOf(100.0), UUID.randomUUID(), LocalDate.now(), "Test");
    }

    @Nested
    class create {
        @Test
        @DisplayName("Deve lançar a exceção CategoryNotFoundException quando a categoria não for encontrada.")
        void shouldThrowTCategoryNotFoundExceptionWhenCategoryNotFound() {
            // Arrange
            when(categoryRepository.findCategoryByIdAndUser(createTransactionRequestDto.categoryId(), user.getId()))
                    .thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(CategoryNotFoundException.class, () -> transactionService.create(createTransactionRequestDto, user));
            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar a exceção TransactionTypeMismatchException quando o tipo de transação não é o mesmo da categoria.")
        void shouldThrowTransactionTypeMismatchExceptionWhenTransactionTypeDoesNotMatchCategoryType() {
            // Arrange
            when(categoryRepository.findCategoryByIdAndUser(createTransactionRequestDto.categoryId(), user.getId()))
                    .thenReturn(Optional.of(category));
            when(category.getType()).thenReturn(CategoryType.INCOME);

            // Act + Assert
            assertThrows(TransactionTypeMismatchException.class, () -> transactionService.create(createTransactionRequestDto, user));
            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve salvar a nova transação com sucesso.")
        void shouldSaveANewTransactionWithSuccessfully() {
            // Arrange
            when(categoryRepository.findCategoryByIdAndUser(createTransactionRequestDto.categoryId(), user.getId()))
                    .thenReturn(Optional.of(category));
            when(category.getType()).thenReturn(CategoryType.EXPENSE);
            when(transaction.getCategory()).thenReturn(category);
            when(transactionRepository.save(any())).thenReturn(transaction);

            // Act
            transactionService.create(createTransactionRequestDto, user);

            // Assert
            verify(transactionRepository).save(transactionArgumentCaptor.capture());

            Transaction transactionCaptured = transactionArgumentCaptor.getValue();

            assertEquals(createTransactionRequestDto.type(), transactionCaptured.getType());
            assertEquals(createTransactionRequestDto.amount(), transactionCaptured.getAmount());
            assertEquals(category, transactionCaptured.getCategory());
            assertEquals(user, transactionCaptured.getUser());
            assertEquals(createTransactionRequestDto.date(), transactionCaptured.getDate());
            assertEquals(createTransactionRequestDto.description(), transactionCaptured.getDescription());
        }
    }

    @Nested
    class listTransactionFromUser {
        @Test
        @DisplayName("Deve retornar todas as transações sem filtro")
        void shouldReturnAllTransactionWithoutFilters() {
            // Arrange
            when(transactionRepository.findAllWithFilters(user.getId(), null, null, null, null, null, null))
                    .thenReturn(List.of(transaction));
            when(transaction.getCategory()).thenReturn(category);

            // Act
            List<ListTransactionsResponseDto> result = transactionService
                    .listTransactionFromUser(user, null, null, null, null, null, null);

            // Assert
            verify(transactionRepository).findAllWithFilters(user.getId(), null, null, null, null, null, null);
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Deve retornar todas as transações com filtros")
        void shouldReturnAllTransactionWithFilters() {
            // Arrange
            TransactionType type = TransactionType.EXPENSE;
            String categoryName = "Alimentação";
            BigDecimal amountGreaterThan = BigDecimal.valueOf(100);
            BigDecimal amountLessThan = BigDecimal.valueOf(500);
            LocalDate startDate = LocalDate.of(2026, 6, 1);
            LocalDate endDate = LocalDate.of(2026, 6, 30);

            when(transactionRepository.findAllWithFilters(user.getId(), type, categoryName, amountGreaterThan, amountLessThan, startDate, endDate))
                    .thenReturn(List.of(transaction));
            when(transaction.getCategory()).thenReturn(category);

            // Act
            List<ListTransactionsResponseDto> result = transactionService
                    .listTransactionFromUser(user, type, categoryName, amountGreaterThan, amountLessThan, startDate, endDate);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Deve retornar uma lista vazio quando não há transações")
        void shouldReturnAnEmptyListWhenThereAreNoTransactions() {
            // Arrange
            when(transactionRepository.findAllWithFilters(user.getId(), null, "Alimentação", null, null, null, null))
                    .thenReturn(List.of());

            // Act
            List<ListTransactionsResponseDto> result = transactionService
                    .listTransactionFromUser(user, null, "Alimentação", null, null, null, null);

            // Assert
            assertNotNull(result);
            assertEquals(0, result.size());
        }
    }

    @Nested
    class transactionDetails {
        @Test
        @DisplayName("Deve lançar a exceção TransactionNotFoundException quando a transação não for encontrada.")
        void shouldThrowTransactionNotFoundExceptionWhenTransactionNotFound() {
            // Arrange
            UUID transactionId = UUID.randomUUID();
            when(transactionRepository.findByIdAndUserId(transactionId, user.getId())).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(TransactionNotFoundException.class, () -> transactionService.transactionDetails(transactionId, user));
            verify(transactionRepository).findByIdAndUserId(transactionId, user.getId());
        }

        @Test
        @DisplayName("Deve retornar uma transação com sucesso")
        void shouldReturnTransactionSuccessfully() {
            // Arrange
            UUID transactionId = UUID.randomUUID();
            when(transactionRepository.findByIdAndUserId(transactionId, user.getId())).thenReturn(Optional.of(transaction));
            when(transaction.getCategory()).thenReturn(category);

            // Act
            transactionService.transactionDetails(transactionId, user);

            verify(transactionRepository).findByIdAndUserId(transactionId, user.getId());
        }
    }

    @Nested
    class updateTransaction {
        @Test
        @DisplayName("Deve lançar a exceção TransactionNotFoundException quando a transação não for encontrada.")
        void shouldThrowTransactionNotFoundExceptionWhenTransactionNotFound() {
            // Arrange
            UUID transactionId = UUID.randomUUID();
            UpdateTransactionRequestDto updateTransactionRequestDto = new UpdateTransactionRequestDto(null, BigDecimal.valueOf(100.00), null, null, null);
            when(transactionRepository.findByIdAndUserId(transactionId, user.getId())).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(TransactionNotFoundException.class, () -> transactionService.updateTransaction(transactionId, user, updateTransactionRequestDto));
            verify(transactionRepository).findByIdAndUserId(transactionId, user.getId());
            verify(transaction, never()).update(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Deve lançar a exceção TransactionTypeMismatchException quando o tipo da transação não corresponde ao tipo da categoria.")
        void shouldThrowTransactionTypeMismatchExceptionWhenTransactionTypeDoesNotMatchTheCategoryType() {
            // Arrange
            UUID transactionId = UUID.randomUUID();
            UpdateTransactionRequestDto updateTransactionRequestDto = new UpdateTransactionRequestDto(TransactionType.EXPENSE, null, null, null, null);

            when(transactionRepository.findByIdAndUserId(transactionId, user.getId())).thenReturn(Optional.of(transaction));
            when(transaction.getType()).thenReturn(TransactionType.INCOME);
            when(transaction.getCategory()).thenReturn(category);
            when(transaction.getCategory().getType()).thenReturn(CategoryType.INCOME);

            // Act + Assert
            assertThrows(TransactionTypeMismatchException.class, () -> transactionService.updateTransaction(transactionId, user, updateTransactionRequestDto));
            verify(transaction, never()).update(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Deve lançar a exceção CategoryNotFoundException quando a categoria não é encontrada.")
        void shouldThrowCategoryNotFoundExceptionWhenCategoryNotFound() {
            // Arrange
            UUID transactionId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            UpdateTransactionRequestDto updateTransactionRequestDto = new UpdateTransactionRequestDto(null, null, categoryId, null, null);

            when(transactionRepository.findByIdAndUserId(transactionId, user.getId())).thenReturn(Optional.of(transaction));
            when(transaction.getType()).thenReturn(TransactionType.INCOME);
            when(categoryRepository.findCategoryByIdAndUser(categoryId, user.getId())).thenReturn(Optional.empty());
//
            // Act + Assert
            assertThrows(CategoryNotFoundException.class, () -> transactionService.updateTransaction(transactionId, user, updateTransactionRequestDto));
            verify(transaction, never()).update(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Deve lançar a exceção TransactionTypeMismatchException quando o tipo da categoria não corresponde ao tipo da transação.")
        void shouldThrowTransactionTypeMismatchExceptionWhenCategoryTypeDoesNotMatchTheTransactionType() {
            // Arrange
            UUID transactionId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            UpdateTransactionRequestDto updateTransactionRequestDto = new UpdateTransactionRequestDto(null, null, categoryId, null, null);

            when(transactionRepository.findByIdAndUserId(transactionId, user.getId())).thenReturn(Optional.of(transaction));
            when(transaction.getType()).thenReturn(TransactionType.INCOME);
            when(categoryRepository.findCategoryByIdAndUser(categoryId, user.getId())).thenReturn(Optional.of(category));
            when(transaction.getCategory()).thenReturn(category);
            when(transaction.getCategory().getType()).thenReturn(CategoryType.EXPENSE);

            // Act + Assert
            assertThrows(TransactionTypeMismatchException.class, () -> transactionService.updateTransaction(transactionId, user, updateTransactionRequestDto));
            verify(transaction, never()).update(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Deve atualizar a transação usando a categoria atual quando nenhuma categoria é informada.")
        void shouldUpdateTransactionUsingCurrentCategoryWhenCategoryIsNotProvided() {
            // Arrange
            UUID transactionId = UUID.randomUUID();
            UpdateTransactionRequestDto updateTransactionRequestDto = new UpdateTransactionRequestDto(
                    TransactionType.EXPENSE, null, null, null, null);

            when(transactionRepository.findByIdAndUserId(transactionId, user.getId())).thenReturn(Optional.of(transaction));
            when(transaction.getCategory()).thenReturn(currentCategory);
            when(currentCategory.getType()).thenReturn(CategoryType.EXPENSE);

            // Act
            transactionService.updateTransaction(transactionId, user, updateTransactionRequestDto);

            // Assert
            verify(categoryRepository, never()).findCategoryByIdAndUser(any(), any());
            verify(transaction).update(
                    TransactionType.EXPENSE,
                    null,
                    currentCategory,
                    null,
                    null
            );
        }

        @Test
        @DisplayName("Deve atualizar tipo e categoria quando ambos forem compatíveis.")
        void shouldUpdateTheTypeAndCategoryWhenBothAreCompatible() {
            // Arrange
            UUID transactionId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            UpdateTransactionRequestDto updateTransactionRequestDto = new UpdateTransactionRequestDto(
                    TransactionType.EXPENSE, null, categoryId, null, null);

            when(transactionRepository.findByIdAndUserId(transactionId, user.getId()))
                    .thenReturn(Optional.of(transaction));
            when(transaction.getType()).thenReturn(TransactionType.INCOME);
            when(transaction.getCategory()).thenReturn(currentCategory);
            when(currentCategory.getType()).thenReturn(CategoryType.INCOME);
            when(categoryRepository.findCategoryByIdAndUser(categoryId, user.getId())).thenReturn(Optional.of(newCategory));
            when(newCategory.getType()).thenReturn(CategoryType.EXPENSE);

            // Act
            transactionService.updateTransaction(transactionId, user, updateTransactionRequestDto);

            // Assert
            verify(categoryRepository).findCategoryByIdAndUser(categoryId, user.getId());
            verify(transaction).update(
                    TransactionType.EXPENSE,
                    null,
                    newCategory,
                    null,
                    null
            );
        }

        @Test
        @DisplayName("Deve atualizar todos os campos da transação")
        void shouldUpdateAllTransactionFields() {
            // Arrange
            UUID transactionId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();

            LocalDate date = LocalDate.of(2026, 6, 23);
            BigDecimal amount = BigDecimal.valueOf(250.75);

            UpdateTransactionRequestDto dto = new UpdateTransactionRequestDto(
                    TransactionType.EXPENSE,
                    amount,
                    categoryId,
                    date,
                    "Compra do mercado"
            );

            when(transactionRepository.findByIdAndUserId(transactionId, user.getId()))
                    .thenReturn(Optional.of(transaction));
            when(transaction.getCategory()).thenReturn(currentCategory);
            when(currentCategory.getType()).thenReturn(CategoryType.INCOME);
            when(categoryRepository.findCategoryByIdAndUser(categoryId, user.getId()))
                    .thenReturn(Optional.of(newCategory));
            when(newCategory.getType()).thenReturn(CategoryType.EXPENSE);

            // Act
            transactionService.updateTransaction(transactionId, user, dto);

            // Assert
            verify(categoryRepository)
                    .findCategoryByIdAndUser(categoryId, user.getId());
            verify(transaction).update(
                    TransactionType.EXPENSE,
                    amount,
                    newCategory,
                    date,
                    "Compra do mercado"
            );
        }

        @Test
        @DisplayName("Deve atualizar somente o valor da transação")
        void shouldUpdateOnlyAmount() {
            // Arrange
            UUID transactionId = UUID.randomUUID();
            BigDecimal amount = BigDecimal.valueOf(250.75);

            UpdateTransactionRequestDto updateTransactionRequestDto = new UpdateTransactionRequestDto(
                    null,
                    amount,
                    null,
                    null,
                    null
            );

            when(transactionRepository.findByIdAndUserId(transactionId, user.getId()))
                    .thenReturn(Optional.of(transaction));
            when(transaction.getType()).thenReturn(TransactionType.EXPENSE);
            when(transaction.getCategory()).thenReturn(category);
            when(category.getType()).thenReturn(CategoryType.EXPENSE);

            // Act
            transactionService.updateTransaction(transactionId, user, updateTransactionRequestDto);

            // Assert
            verify(categoryRepository, never())
                    .findCategoryByIdAndUser(any(), any());
            verify(transaction).update(
                    null,
                    amount,
                    category,
                    null,
                    null
            );
        }
    }

    @Nested
    class deleteTransaction {
        @Test
        @DisplayName("Deve lançar a exceção TransactionNotFoundException quando a transação não for encontrada.")
        void shouldThrowTransactionNotFoundExceptionWhenTransactionNotFound() {
            // Arrange
            UUID transactionId = UUID.randomUUID();
            when(transactionRepository.findByIdAndUserId(transactionId, user.getId())).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(TransactionNotFoundException.class, () -> transactionService.deleteTransaction(transactionId, user));
            verify(transactionRepository).findByIdAndUserId(transactionId, user.getId());
        }

        @Test
        @DisplayName("Deve deletar a transação com sucesso.")
        void shouldDeleteTransaction() {
            // Arrange
            UUID transactionId = UUID.randomUUID();
            when(transactionRepository.findByIdAndUserId(transactionId, user.getId())).thenReturn(Optional.of(transaction));

            // Act
            transactionService.deleteTransaction(transactionId, user);

            // Assert
            verify(transactionRepository).findByIdAndUserId(transactionId, user.getId());
            verify(transactionRepository).delete(transaction);
        }
    }
}