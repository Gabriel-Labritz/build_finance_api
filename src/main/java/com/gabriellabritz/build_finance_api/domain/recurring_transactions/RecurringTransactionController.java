package com.gabriellabritz.build_finance_api.domain.recurring_transactions;

import com.gabriellabritz.build_finance_api.domain.recurring_transactions.dtos.request.CreateRecurringTransactionRequestDto;
import com.gabriellabritz.build_finance_api.domain.recurring_transactions.dtos.request.UpdateRecurringTransactionRequestDto;
import com.gabriellabritz.build_finance_api.domain.recurring_transactions.dtos.response.RecurringTransactionDetailsResponseDto;
import com.gabriellabritz.build_finance_api.domain.recurring_transactions.dtos.response.RecurringTransactionResponseDto;
import com.gabriellabritz.build_finance_api.domain.recurring_transactions.enums.Frequency;
import com.gabriellabritz.build_finance_api.domain.transactions.enums.TransactionType;
import com.gabriellabritz.build_finance_api.domain.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/recurring-transactions")
public class RecurringTransactionController {
    private final RecurringTransactionService recurringTransactionService;

    public RecurringTransactionController(RecurringTransactionService recurringTransactionService) {
        this.recurringTransactionService = recurringTransactionService;
    }

    @PostMapping
    public ResponseEntity<RecurringTransactionResponseDto> create(
            @AuthenticationPrincipal User userLogged,
            @RequestBody @Valid CreateRecurringTransactionRequestDto dto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(recurringTransactionService.createRecurringTransaction(userLogged, dto));
    }

    @GetMapping
    public ResponseEntity<List<RecurringTransactionResponseDto>> listAllRecurringTransactions(
            @AuthenticationPrincipal User userLogged,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Frequency frequency,
            @RequestParam(required = false) Boolean active
            ) {
        return ResponseEntity.ok().body(recurringTransactionService.listAllRecurringTransactions(userLogged, type, category, frequency, active));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecurringTransactionDetailsResponseDto> getRecurringTransactionDetails(
            @PathVariable UUID id,
            @AuthenticationPrincipal User userLogged
            ) {
        return ResponseEntity.ok().body(recurringTransactionService.getRecurringTransactionDetails(id, userLogged));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecurringTransactionResponseDto> updateRecurringTransaction(
            @PathVariable UUID id,
            @AuthenticationPrincipal User userLogged,
            @RequestBody @Valid UpdateRecurringTransactionRequestDto dto
    ) {
        return ResponseEntity.ok().body(recurringTransactionService.updateRecurringTransaction(id, userLogged, dto));
    }

    @PatchMapping("/activate/{id}")
    public ResponseEntity<Void> activateRecurringTransaction(
            @PathVariable UUID id,
            @AuthenticationPrincipal User userLogged
    ) {
        recurringTransactionService.activateRecurringTransaction(id, userLogged);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/disable/{id}")
    public ResponseEntity<Void> disableRecurringTransaction(
            @PathVariable UUID id,
            @AuthenticationPrincipal User userLogged
    ) {
        recurringTransactionService.disableRecurringTransaction(id, userLogged);
        return ResponseEntity.noContent().build();
    }
}
