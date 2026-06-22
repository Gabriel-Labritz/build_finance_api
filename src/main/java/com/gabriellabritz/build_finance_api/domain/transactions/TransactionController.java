package com.gabriellabritz.build_finance_api.domain.transactions;

import com.gabriellabritz.build_finance_api.domain.transactions.dtos.request.CreateTransactionRequestDto;
import com.gabriellabritz.build_finance_api.domain.transactions.dtos.request.UpdateTransactionRequestDto;
import com.gabriellabritz.build_finance_api.domain.transactions.dtos.response.ListTransactionsResponseDto;
import com.gabriellabritz.build_finance_api.domain.transactions.dtos.response.TransactionDetailsResponseDto;
import com.gabriellabritz.build_finance_api.domain.transactions.dtos.response.TransactionResponseDto;
import com.gabriellabritz.build_finance_api.domain.transactions.enums.TransactionType;
import com.gabriellabritz.build_finance_api.domain.user.User;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponseDto> create(
            @RequestBody @Valid CreateTransactionRequestDto createTransactionRequestDto,
            @AuthenticationPrincipal User userLogged
    ) {
        return ResponseEntity.ok().body(transactionService.create(createTransactionRequestDto, userLogged));
    }

    @GetMapping ResponseEntity<List<ListTransactionsResponseDto>> listTransactionFromUser(
            @AuthenticationPrincipal User userLogged,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) BigDecimal amountGreaterThan,
            @RequestParam(required = false) BigDecimal amountLessThan,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate endDate
            ) {
            return ResponseEntity.ok().body(transactionService.listTransactionFromUser(
                    userLogged,
                    type,
                    categoryName,
                    amountGreaterThan,
                    amountLessThan,
                    startDate,
                    endDate
            ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDetailsResponseDto> transactionDetails(@PathVariable UUID id, @AuthenticationPrincipal User userLogged) {
        return ResponseEntity.ok().body(transactionService.transactionDetails(id, userLogged));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponseDto> updateTransaction(
            @PathVariable UUID id,
            @AuthenticationPrincipal User userLogged,
            @RequestBody UpdateTransactionRequestDto updateTransactionRequestDto
    ) {
        return ResponseEntity.ok().body(transactionService.updateTransaction(id, userLogged, updateTransactionRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteTransaction(@PathVariable UUID id, @AuthenticationPrincipal User userLogged) {
        transactionService.deleteTransaction(id, userLogged);
        return ResponseEntity.noContent().build();
    }
}
