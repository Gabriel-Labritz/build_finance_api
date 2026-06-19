package com.gabriellabritz.build_finance_api.domain.transactions;

import com.gabriellabritz.build_finance_api.domain.categories.Category;
import com.gabriellabritz.build_finance_api.domain.categories.CategoryRepository;
import com.gabriellabritz.build_finance_api.domain.transactions.dtos.request.CreateTransactionRequestDto;
import com.gabriellabritz.build_finance_api.domain.transactions.dtos.response.TransactionResponseDto;
import com.gabriellabritz.build_finance_api.domain.user.User;
import com.gabriellabritz.build_finance_api.infra.exceptions.categories.CategoryNotFoundException;
import com.gabriellabritz.build_finance_api.infra.exceptions.transactions.TransactionTypeMismatchException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

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
        Category category = categoryRepository.findCategoryByIdAndUser(createTransactionRequestDto.categoryId(), userLogged.getId())
                .orElseThrow(() -> new CategoryNotFoundException("A categoria informada não foi encontrada"));

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
}
