package com.gabriellabritz.build_finance_api.domain.recurring_transactions;

import com.gabriellabritz.build_finance_api.domain.categories.Category;
import com.gabriellabritz.build_finance_api.domain.categories.CategoryRepository;
import com.gabriellabritz.build_finance_api.domain.recurring_transactions.dtos.request.CreateRecurringTransactionRequestDto;
import com.gabriellabritz.build_finance_api.domain.recurring_transactions.dtos.request.UpdateRecurringTransactionRequestDto;
import com.gabriellabritz.build_finance_api.domain.recurring_transactions.dtos.response.RecurringTransactionDetailsResponseDto;
import com.gabriellabritz.build_finance_api.domain.recurring_transactions.dtos.response.RecurringTransactionResponseDto;
import com.gabriellabritz.build_finance_api.domain.recurring_transactions.enums.Frequency;
import com.gabriellabritz.build_finance_api.domain.transactions.enums.TransactionType;
import com.gabriellabritz.build_finance_api.domain.user.User;
import com.gabriellabritz.build_finance_api.infra.exceptions.categories.CategoryNotFoundException;
import com.gabriellabritz.build_finance_api.infra.exceptions.recurring_transactions.RecurringTransactionNotFoundException;
import com.gabriellabritz.build_finance_api.infra.exceptions.transactions.TransactionTypeMismatchException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RecurringTransactionService {
    private final RecurringTransactionRepository recurringTransactionRepository;
    private final CategoryRepository categoryRepository;

    public RecurringTransactionService(RecurringTransactionRepository recurringTransactionRepository, CategoryRepository categoryRepository) {
        this.recurringTransactionRepository = recurringTransactionRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public RecurringTransactionResponseDto createRecurringTransaction(User userLogged, CreateRecurringTransactionRequestDto dto) {
        Category category = getCategory(userLogged.getId(), dto.categoryId());

        if (!dto.type().name().equals(category.getType().name())) {
            throw new TransactionTypeMismatchException("O tipo da transação não corresponde ao tipo da categoria.");
        }

        RecurringTransaction.validateStartDate(dto.startDate());
        Optional.ofNullable(dto.endDate())
                .ifPresent(endDate -> RecurringTransaction.validateEndDate(dto.startDate(), endDate));

        RecurringTransaction recurringTransaction = recurringTransactionRepository.save(new RecurringTransaction(
                dto.title(),
                dto.description(),
                dto.amount(),
                dto.type(),
                category,
                dto.frequency(),
                dto.startDate(),
                dto.endDate(),
                userLogged
        ));

        return new RecurringTransactionResponseDto(recurringTransaction);
    }

    public List<RecurringTransactionResponseDto> listAllRecurringTransactions(
            User userLogged, TransactionType type,
            String category, Frequency frequency,
            Boolean active) {
        return recurringTransactionRepository
                .findAllRecurringTransaction(userLogged.getId(), type, category, frequency, active)
                .stream().map(RecurringTransactionResponseDto::new)
                .toList();
    }

    public RecurringTransactionDetailsResponseDto getRecurringTransactionDetails(UUID id, User userLogged) {
        RecurringTransaction recurringTransaction = getRecurringTransaction(id, userLogged.getId());
        return new RecurringTransactionDetailsResponseDto(recurringTransaction);
    }

    @Transactional
    public RecurringTransactionResponseDto updateRecurringTransaction(UUID id, User userLogged, UpdateRecurringTransactionRequestDto dto) {
        RecurringTransaction recurringTransaction = getRecurringTransaction(id, userLogged.getId());

        TransactionType finalType = Optional.ofNullable(dto.type())
                .orElse(recurringTransaction.getType());

        Category finalCategory = Optional.ofNullable(dto.categoryId())
                .map(categoryId -> getCategory(userLogged.getId(), categoryId))
                .orElse(recurringTransaction.getCategory());

        if(!finalType.name().equals(finalCategory.getType().name())) {
            throw new TransactionTypeMismatchException("O tipo da transação não corresponde ao tipo da categoria.");
        }

        Optional.ofNullable(dto.endDate())
                .ifPresent(endDate -> RecurringTransaction.validateEndDate(recurringTransaction.getStartDate(), endDate));

        recurringTransaction.update(
                dto.title(),
                dto.description(),
                dto.amount(),
                dto.type(),
                finalCategory,
                dto.frequency(),
                dto.endDate()
        );

        return new RecurringTransactionResponseDto(recurringTransaction);
    }

    @Transactional
    public void disableRecurringTransaction(UUID id, User userLogged) {
        RecurringTransaction recurringTransaction = getRecurringTransaction(id, userLogged.getId());
        recurringTransaction.disable();
    }

    @Transactional
    public void activateRecurringTransaction(UUID id, User userLogged) {
        RecurringTransaction recurringTransaction = getRecurringTransaction(id, userLogged.getId());
        recurringTransaction.activate();
    }

    private RecurringTransaction getRecurringTransaction(UUID id, UUID userId) {
        return recurringTransactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RecurringTransactionNotFoundException("A transação recorrente não foi encontrada."));
    }

    private Category getCategory(UUID userId, UUID categoryId) {
        return categoryRepository.findCategoryByIdAndUser(categoryId, userId)
                .orElseThrow(() -> new CategoryNotFoundException("A categoria informada não foi encontrada."));
    }
}
