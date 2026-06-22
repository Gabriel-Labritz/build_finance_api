package com.gabriellabritz.build_finance_api.domain.transactions;

import com.gabriellabritz.build_finance_api.domain.categories.Category;
import com.gabriellabritz.build_finance_api.domain.categories.CategoryRepository;
import com.gabriellabritz.build_finance_api.domain.transactions.dtos.request.CreateTransactionRequestDto;
import com.gabriellabritz.build_finance_api.domain.transactions.dtos.request.UpdateTransactionRequestDto;
import com.gabriellabritz.build_finance_api.domain.transactions.dtos.response.ListTransactionsResponseDto;
import com.gabriellabritz.build_finance_api.domain.transactions.dtos.response.TransactionDetailsResponseDto;
import com.gabriellabritz.build_finance_api.domain.transactions.dtos.response.TransactionResponseDto;
import com.gabriellabritz.build_finance_api.domain.transactions.enums.TransactionType;
import com.gabriellabritz.build_finance_api.domain.user.User;
import com.gabriellabritz.build_finance_api.infra.exceptions.categories.CategoryNotFoundException;
import com.gabriellabritz.build_finance_api.infra.exceptions.transactions.TransactionNotFoundException;
import com.gabriellabritz.build_finance_api.infra.exceptions.transactions.TransactionTypeMismatchException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(CategoryRepository categoryRepository, TransactionRepository transactionRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransactionResponseDto create(CreateTransactionRequestDto createTransactionRequestDto, User userLogged) {
        Category category = getCategory(createTransactionRequestDto.categoryId(), userLogged.getId());

        if(!createTransactionRequestDto.type().name().equals(category.getType().name())) {
            throw new TransactionTypeMismatchException("O tipo da transação não corresponde ao tipo da categoria.");
        }

        Transaction transaction = transactionRepository.save(new Transaction(
                createTransactionRequestDto.type(),
                createTransactionRequestDto.amount(),
                category,
                userLogged,
                createTransactionRequestDto.date(),
                createTransactionRequestDto.description()
        ));

        return new TransactionResponseDto(transaction);
    }

    public List<ListTransactionsResponseDto> listTransactionFromUser(
            User userLogged,
            TransactionType type,
            String categoryName,
            BigDecimal amountGreaterThan,
            BigDecimal amountLessThan,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return transactionRepository.findAllWithFilters
                (userLogged.getId(), type, categoryName, amountGreaterThan, amountLessThan, startDate, endDate)
                .stream().map(ListTransactionsResponseDto::new)
                .toList();
    }

    public TransactionDetailsResponseDto transactionDetails(UUID id, User userLogged) {
        Transaction transaction = getTransaction(id, userLogged.getId());
        return new TransactionDetailsResponseDto(transaction);
    }

    @Transactional
    public TransactionResponseDto updateTransaction(
            UUID id,
            User userLogged,
            UpdateTransactionRequestDto updateTransactionRequestDto
    ) {
        Transaction transaction = getTransaction(id, userLogged.getId());

        TransactionType finalType = Optional.ofNullable(updateTransactionRequestDto.type()).orElse(transaction.getType());

        Category finalCategory = Optional.ofNullable(updateTransactionRequestDto.categoryId())
                .map(categoryId -> getCategory(categoryId, userLogged.getId())).orElse(transaction.getCategory());

        if(!finalType.name().equals(finalCategory.getType().name())) {
            throw new TransactionTypeMismatchException("O tipo da transação não corresponde ao tipo da categoria.");
        }

        transaction.update(
                updateTransactionRequestDto.type(),
                updateTransactionRequestDto.amount(),
                finalCategory,
                updateTransactionRequestDto.date(),
                updateTransactionRequestDto.description()
        );

        return new TransactionResponseDto(transaction);
    }

    @Transactional
    public void deleteTransaction(UUID id, User userLogged) {
        Transaction transaction = getTransaction(id, userLogged.getId());
        transactionRepository.delete(transaction);
    }

    private Transaction getTransaction(UUID id, UUID userId) {
        return transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new TransactionNotFoundException("A transação não foi encontrada."));
    }

    private Category getCategory(UUID categoryId, UUID userId) {
        return categoryRepository.findCategoryByIdAndUser(categoryId, userId)
                .orElseThrow(() -> new CategoryNotFoundException("A categoria informada não foi encontrada"));
    }
}
