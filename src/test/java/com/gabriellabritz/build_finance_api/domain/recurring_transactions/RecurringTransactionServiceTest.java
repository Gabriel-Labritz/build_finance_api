package com.gabriellabritz.build_finance_api.domain.recurring_transactions;

import com.gabriellabritz.build_finance_api.domain.categories.Category;
import com.gabriellabritz.build_finance_api.domain.categories.CategoryRepository;
import com.gabriellabritz.build_finance_api.domain.categories.enums.CategoryType;
import com.gabriellabritz.build_finance_api.domain.recurring_transactions.dtos.request.CreateRecurringTransactionRequestDto;
import com.gabriellabritz.build_finance_api.domain.recurring_transactions.dtos.request.UpdateRecurringTransactionRequestDto;
import com.gabriellabritz.build_finance_api.domain.recurring_transactions.dtos.response.RecurringTransactionResponseDto;
import com.gabriellabritz.build_finance_api.domain.recurring_transactions.enums.Frequency;
import com.gabriellabritz.build_finance_api.domain.transactions.Transaction;
import com.gabriellabritz.build_finance_api.domain.transactions.TransactionRepository;
import com.gabriellabritz.build_finance_api.domain.transactions.enums.TransactionType;
import com.gabriellabritz.build_finance_api.domain.user.User;
import com.gabriellabritz.build_finance_api.infra.exceptions.categories.CategoryNotFoundException;
import com.gabriellabritz.build_finance_api.infra.exceptions.recurring_transactions.InvalidRecurringTransactionDateException;
import com.gabriellabritz.build_finance_api.infra.exceptions.recurring_transactions.RecurringTransactionAlreadyActiveException;
import com.gabriellabritz.build_finance_api.infra.exceptions.recurring_transactions.RecurringTransactionAlreadyInactiveException;
import com.gabriellabritz.build_finance_api.infra.exceptions.recurring_transactions.RecurringTransactionNotFoundException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecurringTransactionServiceTest {
    @InjectMocks
    private RecurringTransactionService recurringTransactionService;

    @Mock
    private User user;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private RecurringTransactionRepository recurringTransactionRepository;

    private CreateRecurringTransactionRequestDto dto;

    @Mock
    private Category category;

    @Mock
    private RecurringTransaction recurringTransactionMock;

    @Captor
    private ArgumentCaptor<RecurringTransaction> recurringTransactionArgumentCaptor;

    @Mock
    private TransactionRepository transactionRepository;

    @Captor
    private ArgumentCaptor<Transaction> transactionArgumentCaptor;

    @BeforeEach
    void setUp() {
        UUID categoryId = UUID.randomUUID();
        this.dto = new CreateRecurringTransactionRequestDto(
                "Test",
                "testing",
                BigDecimal.valueOf(100.0),
                TransactionType.EXPENSE,
                categoryId,
                Frequency.MONTHLY,
                LocalDate.now(),
                LocalDate.now().plusMonths(1)
        );
    }

    @Nested
    class createRecurringTransaction {
        @Test
        @DisplayName("Deve lançar a exceção CategoryNotFoundException quando a categoria informada não é encontrada.")
        void shouldThrowCategoryNotFoundExceptionWhenCategoryIsNotFound() {
            // Arrange
            when(categoryRepository.findCategoryByIdAndUser(dto.categoryId(), user.getId()))
                    .thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(CategoryNotFoundException.class,
                    () -> recurringTransactionService.createRecurringTransaction(user, dto));
            verify(recurringTransactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar a exceção TransactionTypeMismatchException quando o tipo de transação recorrente não é o mesmo da categoria.")
        void shouldThrowTransactionTypeMismatchExceptionWhenRecurringTransactionTypeDoesNotMatchCategoryType() {
            // Arrange
            when(categoryRepository.findCategoryByIdAndUser(dto.categoryId(), user.getId()))
                    .thenReturn(Optional.of(category));
            when(category.getType()).thenReturn(CategoryType.INCOME);

            // Act + Assert
            assertThrows(TransactionTypeMismatchException.class,
                    () -> recurringTransactionService.createRecurringTransaction(user, dto));
            verify(recurringTransactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar a exceção InvalidRecurringTransactionDateException quando a data de início é anterior a data atual.")
        void shouldThrowInvalidRecurringTransactionDateExceptionWhenStartDateIsBeforeCurrentDate() {
            // Arrange
            CreateRecurringTransactionRequestDto dtoWithPastDate = new CreateRecurringTransactionRequestDto(
                    "Netflix",
                    null,
                    BigDecimal.valueOf(49.90),
                    TransactionType.EXPENSE,
                    UUID.randomUUID(),
                    Frequency.MONTHLY,
                    LocalDate.now().minusDays(1),
                    null
            );
            when(categoryRepository.findCategoryByIdAndUser(dtoWithPastDate.categoryId(), user.getId()))
                    .thenReturn(Optional.of(category));
            when(category.getType()).thenReturn(CategoryType.EXPENSE);

            // Act + Assert
            assertThrows(InvalidRecurringTransactionDateException.class,
                    () -> recurringTransactionService.createRecurringTransaction(user, dtoWithPastDate));
            verify(recurringTransactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar a exceção InvalidRecurringTransactionDateException quando a data de final é anterior a data de início.")
        void shouldThrowInvalidRecurringTransactionDateExceptionWhenEndDateIsBeforeStartDate() {
            // Arrange
            CreateRecurringTransactionRequestDto dtoWithPastDate = new CreateRecurringTransactionRequestDto(
                    "Netflix",
                    null,
                    BigDecimal.valueOf(49.90),
                    TransactionType.EXPENSE,
                    UUID.randomUUID(),
                    Frequency.MONTHLY,
                    LocalDate.now(),
                    LocalDate.now().minusDays(3)
            );
            when(categoryRepository.findCategoryByIdAndUser(dtoWithPastDate.categoryId(), user.getId()))
                    .thenReturn(Optional.of(category));
            when(category.getType()).thenReturn(CategoryType.EXPENSE);

            // Act + Assert
            assertThrows(InvalidRecurringTransactionDateException.class,
                    () -> recurringTransactionService.createRecurringTransaction(user, dtoWithPastDate));
            verify(recurringTransactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve salvar uma nova transação recorrente com sucesso.")
        void shouldSaveANewRecurringTransactionWithSuccessfully() {
            // Arrange
            when(categoryRepository.findCategoryByIdAndUser(dto.categoryId(), user.getId()))
                    .thenReturn(Optional.of(category));
            when(category.getType()).thenReturn(CategoryType.EXPENSE);
            when(recurringTransactionMock.getCategory()).thenReturn(category);
            when(recurringTransactionRepository.save(any()))
                    .thenReturn(recurringTransactionMock);

            // Act
            recurringTransactionService.createRecurringTransaction(user, dto);

            // Assert
            verify(categoryRepository).findCategoryByIdAndUser(dto.categoryId(), user.getId());
            verify(recurringTransactionRepository).save(recurringTransactionArgumentCaptor.capture());

            RecurringTransaction recurringTransactionCaptured = recurringTransactionArgumentCaptor.getValue();

            assertEquals("Test", recurringTransactionCaptured.getTitle());
            assertEquals("testing", recurringTransactionCaptured.getDescription());
            assertEquals(BigDecimal.valueOf(100.0), recurringTransactionCaptured.getAmount());
            assertEquals(TransactionType.EXPENSE, recurringTransactionCaptured.getType());
            assertEquals(category, recurringTransactionCaptured.getCategory());
            assertEquals(Frequency.MONTHLY, recurringTransactionCaptured.getFrequency());
            assertEquals(LocalDate.now(), recurringTransactionCaptured.getStartDate());
            assertEquals(LocalDate.now().plusMonths(1), recurringTransactionCaptured.getEndDate());
            assertEquals(user, recurringTransactionCaptured.getUser());
        }
    }

    @Nested
    class listAllRecurringTransactions {
        @Test
        @DisplayName("Deve listar todas as transações recorrentes sem filtros aplicados.")
        void shouldListAllRecurringTransactionsWithoutFiltersApplied() {
            // Arrange
            when(recurringTransactionRepository.findAllRecurringTransaction(user.getId(), null, null, null, null))
                    .thenReturn(List.of(recurringTransactionMock));
            when(recurringTransactionMock.getCategory()).thenReturn(category);

            // Act
            List<RecurringTransactionResponseDto> result = recurringTransactionService
                            .listAllRecurringTransactions(user, null, null, null, null);

            // Assert
            verify(recurringTransactionRepository)
                    .findAllRecurringTransaction(user.getId(), null, null, null, null);
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Deve listar todas as transações recorrentes com filtros aplicados.")
        void shouldListAllRecurringTransactionsWithFiltersApplied() {
            // Arrange
            when(recurringTransactionRepository.findAllRecurringTransaction(user.getId(), TransactionType.EXPENSE, null, null, true))
                    .thenReturn(List.of(recurringTransactionMock));
            when(recurringTransactionMock.getCategory()).thenReturn(category);

            // Act
            List<RecurringTransactionResponseDto> result = recurringTransactionService
                    .listAllRecurringTransactions(user, TransactionType.EXPENSE, null, null, true);

            // Assert
            verify(recurringTransactionRepository)
                    .findAllRecurringTransaction(user.getId(), TransactionType.EXPENSE, null, null, true);
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Deve retornar uma lista vazia quando a lista de transações recorrentes está vazia.")
        void shouldReturnEmptyListWhenRecurringTransactionListIsEmpty() {
            // Arrange
            when(recurringTransactionRepository.findAllRecurringTransaction(user.getId(), null, null, null, null))
                    .thenReturn(List.of());

            // Act
            List<RecurringTransactionResponseDto> result = recurringTransactionService
                    .listAllRecurringTransactions(user, null, null, null, null);

            // Assert
            verify(recurringTransactionRepository)
                    .findAllRecurringTransaction(user.getId(), null, null, null, null);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class getRecurringTransactionDetails {
        @Test
        @DisplayName("Deve lançar uma exceção RecurringTransactionNotFoundException quando a transação recorrente não for encontrada.")
        void shouldThrowRecurringTransactionNotFoundExceptionWhenRecurringTransactionIsNotFound() {
            // Arrange
            UUID recurringTransactionId = UUID.randomUUID();

            when(recurringTransactionRepository.findByIdAndUserId(recurringTransactionId, user.getId()))
                    .thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(RecurringTransactionNotFoundException.class,
                    () -> recurringTransactionService.getRecurringTransactionDetails(recurringTransactionId, user));
            verify(recurringTransactionRepository).findByIdAndUserId(recurringTransactionId, user.getId());
        }

        @Test
        @DisplayName("Deve retornar os detalhes da transação recorrente quando ela é encontrada.")
        void shouldReturnRecurringTransactionDetailsWhenRecurringTransactionIsFound() {
            // Arrange
            UUID recurringTransactionId = UUID.randomUUID();

            when(recurringTransactionRepository.findByIdAndUserId(recurringTransactionId, user.getId()))
                    .thenReturn(Optional.of(recurringTransactionMock));
            when(recurringTransactionMock.getCategory()).thenReturn(category);

            // Act
            recurringTransactionService.getRecurringTransactionDetails(recurringTransactionId, user);

            // Assert
            verify(recurringTransactionRepository).findByIdAndUserId(recurringTransactionId, user.getId());
        }
    }

    @Nested
    class disableRecurringTransaction {
        @Test
        @DisplayName("Deve lançar uma exceção RecurringTransactionNotFoundException quando a transação recorrente não for encontrada.")
        void shouldThrowRecurringTransactionNotFoundExceptionWhenRecurringTransactionIsNotFound() {
            // Arrange
            UUID recurringTransactionId = UUID.randomUUID();

            when(recurringTransactionRepository.findByIdAndUserId(recurringTransactionId, user.getId()))
                    .thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(RecurringTransactionNotFoundException.class,
                    () -> recurringTransactionService.disableRecurringTransaction(recurringTransactionId, user));
            verify(recurringTransactionRepository).findByIdAndUserId(recurringTransactionId, user.getId());
        }

        @Test
        @DisplayName("Deve lançar uma exceção RecurringTransactionAlreadyInactiveException quando a transação recorrente já está desabilitada.")
        void shouldThrowRecurringTransactionAlreadyInactiveExceptionWhenRecurringTransactionIsAlreadyDisabled() {
            // Arrange
            UUID recurringTransactionId = UUID.randomUUID();

            when(recurringTransactionRepository.findByIdAndUserId(recurringTransactionId, user.getId()))
                    .thenReturn(Optional.of(recurringTransactionMock));
            doThrow(new RecurringTransactionAlreadyInactiveException("A transação recorrente já está desativada."))
                    .when(recurringTransactionMock)
                    .disable();

            // Act + Assert
            assertThrows(RecurringTransactionAlreadyInactiveException.class,
                    () -> recurringTransactionService.disableRecurringTransaction(recurringTransactionId, user));
            verify(recurringTransactionRepository).findByIdAndUserId(recurringTransactionId, user.getId());
            verify(recurringTransactionMock).disable();
        }

        @Test
        @DisplayName("Deve desabilitar uma transação recorrente.")
        void shouldDisableRecurringTransaction() {
            // Arrange
            UUID recurringTransactionId = UUID.randomUUID();

            when(recurringTransactionRepository.findByIdAndUserId(recurringTransactionId, user.getId()))
                    .thenReturn(Optional.of(recurringTransactionMock));

            // Act
            recurringTransactionService.disableRecurringTransaction(recurringTransactionId, user);

            // Assert
            verify(recurringTransactionRepository).findByIdAndUserId(recurringTransactionId, user.getId());
            verify(recurringTransactionMock).disable();
        }
    }

    @Nested
    class activateRecurringTransaction {
        @Test
        @DisplayName("Deve lançar uma exceção RecurringTransactionNotFoundException quando a transação recorrente não for encontrada.")
        void shouldThrowRecurringTransactionNotFoundExceptionWhenRecurringTransactionIsNotFound() {
            // Arrange
            UUID recurringTransactionId = UUID.randomUUID();

            when(recurringTransactionRepository.findByIdAndUserId(recurringTransactionId, user.getId()))
                    .thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(RecurringTransactionNotFoundException.class,
                    () -> recurringTransactionService.activateRecurringTransaction(recurringTransactionId, user));
            verify(recurringTransactionRepository).findByIdAndUserId(recurringTransactionId, user.getId());
        }

        @Test
        @DisplayName("Deve lançar uma exceção RecurringTransactionAlreadyActiveException quando a transação recorrente já está ativa.")
        void shouldThrowRecurringTransactionAlreadyActiveExceptionWhenRecurringTransactionIsAlreadyActivate() {
            // Arrange
            UUID recurringTransactionId = UUID.randomUUID();

            when(recurringTransactionRepository.findByIdAndUserId(recurringTransactionId, user.getId()))
                    .thenReturn(Optional.of(recurringTransactionMock));
            doThrow(new RecurringTransactionAlreadyActiveException("A transação recorrente já está ativa."))
                    .when(recurringTransactionMock)
                    .activate();

            // Act + Assert
            assertThrows(RecurringTransactionAlreadyActiveException.class,
                    () -> recurringTransactionService.activateRecurringTransaction(recurringTransactionId, user));
            verify(recurringTransactionRepository).findByIdAndUserId(recurringTransactionId, user.getId());
            verify(recurringTransactionMock).activate();
        }

        @Test
        @DisplayName("Deve ativar uma transação recorrente.")
        void shouldActivateRecurringTransaction() {
            // Arrange
            UUID recurringTransactionId = UUID.randomUUID();

            when(recurringTransactionRepository.findByIdAndUserId(recurringTransactionId, user.getId()))
                    .thenReturn(Optional.of(recurringTransactionMock));

            // Act
            recurringTransactionService.activateRecurringTransaction(recurringTransactionId, user);

            // Assert
            verify(recurringTransactionRepository).findByIdAndUserId(recurringTransactionId, user.getId());
            verify(recurringTransactionMock).activate();
        }
    }

    @Nested
    class updateRecurringTransaction {
        @Test
        @DisplayName("Deve lançar uma exceção RecurringTransactionNotFoundException quando a transação recorrente não for encontrada.")
        void shouldThrowRecurringTransactionNotFoundExceptionWhenRecurringTransactionIsNotFound() {
            // Arrange
            UUID recurringTransactionId = UUID.randomUUID();
            UpdateRecurringTransactionRequestDto updateRecurringTransactionRequestDto = new UpdateRecurringTransactionRequestDto(
                    "teste-update",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            when(recurringTransactionRepository.findByIdAndUserId(recurringTransactionId, user.getId()))
                    .thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(RecurringTransactionNotFoundException.class,
                    () -> recurringTransactionService.updateRecurringTransaction(recurringTransactionId, user, updateRecurringTransactionRequestDto));
            verify(recurringTransactionRepository).findByIdAndUserId(recurringTransactionId, user.getId());
        }

        @Test
        @DisplayName("Deve lançar uma exceção CategoryNotFoundException quando a categoria enviada não for encontrada.")
        void shouldThrowCategoryNotFoundExceptionWhenSubmittedCategoryNotFound() {
            // Arrange
            UUID recurringTransactionId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            UpdateRecurringTransactionRequestDto updateRecurringTransactionRequestDto = new UpdateRecurringTransactionRequestDto(
                    null,
                    null,
                    null,
                    null,
                    categoryId,
                    null,
                    null
            );
            when(recurringTransactionRepository.findByIdAndUserId(recurringTransactionId, user.getId()))
                    .thenReturn(Optional.of(recurringTransactionMock));
            when(recurringTransactionMock.getType()).thenReturn(TransactionType.EXPENSE);
            when(categoryRepository.findCategoryByIdAndUser(updateRecurringTransactionRequestDto.categoryId(), user.getId()))
                    .thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(CategoryNotFoundException.class,
                    () -> recurringTransactionService.updateRecurringTransaction(recurringTransactionId, user, updateRecurringTransactionRequestDto));
            verify(categoryRepository).findCategoryByIdAndUser(categoryId, user.getId());
        }

        @Test
        @DisplayName("Deve lançar uma exceção TransactionTypeMismatchException quando o tipo da categoria não corresponde ao tipo da transação recorrente.")
        void shouldThrowTransactionTypeMismatchExceptionWhenCategoryTypeDoesNotMatchRecurringTransactionType() {
            // Arrange
            UUID recurringTransactionId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            UpdateRecurringTransactionRequestDto updateRecurringTransactionRequestDto = new UpdateRecurringTransactionRequestDto(
                    null,
                    null,
                    null,
                    null,
                    categoryId,
                    null,
                    null
            );
            when(recurringTransactionRepository.findByIdAndUserId(recurringTransactionId, user.getId()))
                    .thenReturn(Optional.of(recurringTransactionMock));
            when(recurringTransactionMock.getType()).thenReturn(TransactionType.EXPENSE);
            when(categoryRepository.findCategoryByIdAndUser(updateRecurringTransactionRequestDto.categoryId(), user.getId()))
                    .thenReturn(Optional.of(category));
            when(category.getType()).thenReturn(CategoryType.INCOME);

            // Act + Assert
            assertThrows(TransactionTypeMismatchException.class,
                    () -> recurringTransactionService.updateRecurringTransaction(recurringTransactionId, user, updateRecurringTransactionRequestDto));
            verify(categoryRepository).findCategoryByIdAndUser(categoryId, user.getId());
            verify(recurringTransactionMock, never())
                    .update(null, null, null, null, category, null, null);
        }

        @Test
        @DisplayName("Deve lançar a exceção InvalidRecurringTransactionDateException quando a data de final é enviada e é anterior a data de início.")
        void shouldThrowInvalidRecurringTransactionDateExceptionWhenFinalDateIsSubmittedAndIsBeforeThanStartDate() {
            // Arrange
            UUID recurringTransactionId = UUID.randomUUID();
            UpdateRecurringTransactionRequestDto updateRecurringTransactionRequestDto = new UpdateRecurringTransactionRequestDto(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    LocalDate.now().minusDays(2)
            );
            when(recurringTransactionRepository.findByIdAndUserId(recurringTransactionId, user.getId()))
                    .thenReturn(Optional.of(recurringTransactionMock));
            when(recurringTransactionMock.getType()).thenReturn(TransactionType.EXPENSE);
            when(recurringTransactionMock.getCategory()).thenReturn(category);
            when(recurringTransactionMock.getStartDate()).thenReturn(LocalDate.now());
            when(category.getType()).thenReturn(CategoryType.EXPENSE);

            // Act + Assert
            assertThrows(InvalidRecurringTransactionDateException.class,
                    () -> recurringTransactionService.updateRecurringTransaction(recurringTransactionId, user, updateRecurringTransactionRequestDto));
            verify(recurringTransactionMock, never())
                    .update(null, null, null, null, null, null, updateRecurringTransactionRequestDto.endDate());
        }

        @Test
        @DisplayName("Deve atualizar a transação recorrente usando a categoria atual quando nenhuma categoria é informada")
        void shouldUpdateRecurringTransactionUsingCurrentCategoryWhenCategoryIdIsNotProvided() {
            // Arrange
            UUID recurringTransactionId = UUID.randomUUID();
            UpdateRecurringTransactionRequestDto updateRecurringTransactionRequestDto = new UpdateRecurringTransactionRequestDto(
                    null,
                    null,
                    null,
                    TransactionType.EXPENSE,
                    null,
                    null,
                    null
            );
            Category currentyCategory = mock(Category.class);

            when(recurringTransactionRepository.findByIdAndUserId(recurringTransactionId, user.getId()))
                    .thenReturn(Optional.of(recurringTransactionMock));
            when(recurringTransactionMock.getCategory()).thenReturn(currentyCategory);
            when(currentyCategory.getType()).thenReturn(CategoryType.EXPENSE);

            // Act
            recurringTransactionService.updateRecurringTransaction(recurringTransactionId, user, updateRecurringTransactionRequestDto);

            // Assert
            verify(categoryRepository, never()).findCategoryByIdAndUser(any(), any());
            verify(recurringTransactionMock).update(
                    null,
                    null,
                    null,
                    TransactionType.EXPENSE,
                    currentyCategory,
                    null,
                    null
            );
        }

        @Test
        @DisplayName("Deve atualizar o tipo e categoria quando ambos forem compatíveis.")
        void shouldUpdateTheTypeAndCategoryWhenBothAreCompatible() {
            // Arrange
            UUID recurringTransactionId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            UpdateRecurringTransactionRequestDto updateRecurringTransactionRequestDto = new UpdateRecurringTransactionRequestDto(
                    null,
                    null,
                    null,
                    TransactionType.EXPENSE,
                    categoryId,
                    null,
                    null
            );
            Category currentyCategory = mock(Category.class);
            Category newCategory = mock(Category.class);

            when(recurringTransactionRepository.findByIdAndUserId(recurringTransactionId, user.getId()))
                    .thenReturn(Optional.of(recurringTransactionMock));
            when(recurringTransactionMock.getType()).thenReturn(TransactionType.INCOME);
            when(recurringTransactionMock.getCategory()).thenReturn(currentyCategory);
            when(currentyCategory.getType()).thenReturn(CategoryType.INCOME);
            when(categoryRepository.findCategoryByIdAndUser(updateRecurringTransactionRequestDto.categoryId(), user.getId()))
                    .thenReturn(Optional.of(newCategory));
            when(newCategory.getType()).thenReturn(CategoryType.EXPENSE);

            // Act
            recurringTransactionService.updateRecurringTransaction(recurringTransactionId, user, updateRecurringTransactionRequestDto);

            // Assert
            verify(categoryRepository).findCategoryByIdAndUser(updateRecurringTransactionRequestDto.categoryId(), user.getId());
            verify(recurringTransactionMock).update(
                    null,
                    null,
                    null,
                    TransactionType.EXPENSE,
                    newCategory,
                    null,
                    null
            );
        }

        @Test
        @DisplayName("Deve atualizar somente o campo enviado")
        void shouldUpdateOnlyFieldSend() {
            // Arrange
            UUID recurringTransactionId = UUID.randomUUID();
            UpdateRecurringTransactionRequestDto updateRecurringTransactionRequestDto = new UpdateRecurringTransactionRequestDto(
                    null,
                    null,
                    BigDecimal.valueOf(67.90),
                    null,
                    null,
                    null,
                    null
            );

            when(recurringTransactionRepository.findByIdAndUserId(recurringTransactionId, user.getId()))
                    .thenReturn(Optional.of(recurringTransactionMock));
            when(recurringTransactionMock.getType()).thenReturn(TransactionType.EXPENSE);
            when(recurringTransactionMock.getCategory()).thenReturn(category);
            when(category.getType()).thenReturn(CategoryType.EXPENSE);

            // Act
            recurringTransactionService.updateRecurringTransaction(recurringTransactionId, user, updateRecurringTransactionRequestDto);

            // Assert
            verify(recurringTransactionMock).update(
                    null,
                    null,
                    BigDecimal.valueOf(67.90),
                    null,
                    category,
                    null,
                    null
            );
        }

        @Test
        @DisplayName("Deve atualizar a transação recorrente quando todos os campos são enviados.")
        void shouldUpdateTheRecurringTransactionWhenAllFieldsIsSend() {
            // Arrange
            UUID recurringTransactionId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();

            LocalDate date = LocalDate.of(2027, 6, 23);
            BigDecimal amount = BigDecimal.valueOf(250.75);

            UpdateRecurringTransactionRequestDto updateRecurringTransactionRequestDto = new UpdateRecurringTransactionRequestDto(
                    "New title",
                    "new description",
                    amount,
                    TransactionType.EXPENSE,
                    categoryId,
                    Frequency.YEARLY,
                    date
            );
            Category currentyCategory = mock(Category.class);
            Category newCategory = mock(Category.class);

            when(recurringTransactionRepository.findByIdAndUserId(recurringTransactionId, user.getId()))
                    .thenReturn(Optional.of(recurringTransactionMock));
            when(recurringTransactionMock.getType()).thenReturn(TransactionType.INCOME);
            when(recurringTransactionMock.getCategory()).thenReturn(currentyCategory);
            when(recurringTransactionMock.getStartDate()).thenReturn(LocalDate.now());
            when(currentyCategory.getType()).thenReturn(CategoryType.INCOME);
            when(categoryRepository.findCategoryByIdAndUser(updateRecurringTransactionRequestDto.categoryId(), user.getId()))
                    .thenReturn(Optional.of(newCategory));
            when(newCategory.getType()).thenReturn(CategoryType.EXPENSE);

            // Act
            recurringTransactionService.updateRecurringTransaction(recurringTransactionId, user, updateRecurringTransactionRequestDto);

            // Assert
            verify(categoryRepository).findCategoryByIdAndUser(updateRecurringTransactionRequestDto.categoryId(), user.getId());
            verify(recurringTransactionMock).update(
                    "New title",
                    "new description",
                    amount,
                    TransactionType.EXPENSE,
                    newCategory,
                    Frequency.YEARLY,
                    date
            );
        }
    }

    @Nested
    class processRecurringTransaction {
        @Test
        @DisplayName("Deve criar a transação e avançar a data sem desativa - la.")
        void shouldCreateTheTransactionAndAdvanceTheDateWithoutDeactivatingIt() {
            // Arrange
            when(recurringTransactionRepository.findByActiveTrueAndNextExecutionDateLessThanEqual(LocalDate.now()))
                    .thenReturn(List.of(recurringTransactionMock));
            when(recurringTransactionMock.getEndDate()).thenReturn(null);
            when(recurringTransactionMock.getType()).thenReturn(TransactionType.EXPENSE);
            when(recurringTransactionMock.getAmount()).thenReturn(new BigDecimal("100.00"));
            when(recurringTransactionMock.getCategory()).thenReturn(category);
            when(recurringTransactionMock.getUser()).thenReturn(user);
            when(recurringTransactionMock.getNextExecutionDate()).thenReturn(LocalDate.now());
            when(recurringTransactionMock.getDescription()).thenReturn("Descrição");

            // Act
            recurringTransactionService.processRecurringTransaction();

            // Assert
            verify(recurringTransactionMock, never()).disable();
            verify(transactionRepository).save(transactionArgumentCaptor.capture());
            verify(recurringTransactionMock).advanceNextDueDate();

            Transaction transactionCaptured = transactionArgumentCaptor.getValue();

            assertEquals(TransactionType.EXPENSE, transactionCaptured.getType());
            assertEquals(new BigDecimal("100.00"), transactionCaptured.getAmount());
            assertEquals(category, transactionCaptured.getCategory());
            assertEquals(LocalDate.now(), transactionCaptured.getDate());
            assertEquals("Descrição", transactionCaptured.getDescription());
        }

        @Test
        @DisplayName("Deve desativar a transação quando a próxima data de execução for depois da data final.")
        void shouldDisableTheRecurringTransactionWhenNextExecutionDateIsAfterThanFinalDate() {
            // Arrange
            when(recurringTransactionRepository.findByActiveTrueAndNextExecutionDateLessThanEqual(LocalDate.now()))
                    .thenReturn(List.of(recurringTransactionMock));
            when(recurringTransactionMock.getEndDate()).thenReturn(LocalDate.of(2026, 7, 2));
            when(recurringTransactionMock.getNextExecutionDate()).thenReturn(LocalDate.of(2026, 8, 3));

            // Act
            recurringTransactionService.processRecurringTransaction();

            // Assert
            verify(recurringTransactionMock).disable();
            verify(transactionRepository, never()).save(any());
            verify(recurringTransactionMock, never()).advanceNextDueDate();
        }

        @Test
        @DisplayName("Não deve processar nenhuma transação quando não houver recorrências vencidas.")
        void shouldNotProcessAnyTransactionsWhenThereAreNoOverdueRecurringPayments() {
            // Arrange
            when(recurringTransactionRepository.findByActiveTrueAndNextExecutionDateLessThanEqual(LocalDate.now()))
                    .thenReturn(List.of());

            // Act
            recurringTransactionService.processRecurringTransaction();

            // Assert
            verify(recurringTransactionMock, never()).disable();
            verify(transactionRepository, never()).save(any());
            verify(recurringTransactionMock, never()).advanceNextDueDate();
        }

        @Test
        @DisplayName("Deve processar corretamente uma lista com transações dentro e fora do prazo.")
        void shouldCorrectlyProcessAListContainingTransactionsThatAreBothWithinAndPastTheDeadline() {
            // Arrange
            RecurringTransaction recurringTransactionMock2 = mock(RecurringTransaction.class);
            when(recurringTransactionRepository.findByActiveTrueAndNextExecutionDateLessThanEqual(LocalDate.now()))
                    .thenReturn(List.of(recurringTransactionMock, recurringTransactionMock2));
            when(recurringTransactionMock.getEndDate()).thenReturn(null);
            when(recurringTransactionMock.getType()).thenReturn(TransactionType.EXPENSE);
            when(recurringTransactionMock.getAmount()).thenReturn(new BigDecimal("100.00"));
            when(recurringTransactionMock.getCategory()).thenReturn(category);
            when(recurringTransactionMock.getUser()).thenReturn(user);
            when(recurringTransactionMock.getNextExecutionDate()).thenReturn(LocalDate.now());
            when(recurringTransactionMock.getDescription()).thenReturn("Descrição");
            when(recurringTransactionMock2.getEndDate()).thenReturn(LocalDate.of(2026, 7, 2));
            when(recurringTransactionMock2.getNextExecutionDate()).thenReturn(LocalDate.of(2026, 8, 3));

            // Act
            recurringTransactionService.processRecurringTransaction();

            // Assert
            verify(recurringTransactionMock, never()).disable();
            verify(recurringTransactionMock).advanceNextDueDate();
            verify(transactionRepository, times(1)).save(transactionArgumentCaptor.capture());

            Transaction transactionCaptured = transactionArgumentCaptor.getValue();

            assertEquals(TransactionType.EXPENSE, transactionCaptured.getType());
            assertEquals(new BigDecimal("100.00"), transactionCaptured.getAmount());
            assertEquals(category, transactionCaptured.getCategory());
            assertEquals(LocalDate.now(), transactionCaptured.getDate());
            assertEquals("Descrição", transactionCaptured.getDescription());

            verify(recurringTransactionMock2).disable();
            verify(recurringTransactionMock2, never()).advanceNextDueDate();
        }
    }
}