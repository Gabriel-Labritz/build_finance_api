package com.gabriellabritz.build_finance_api.domain.transactions;

import com.gabriellabritz.build_finance_api.domain.transactions.dtos.request.CreateTransactionRequestDto;
import com.gabriellabritz.build_finance_api.domain.transactions.dtos.response.TransactionResponseDto;
import com.gabriellabritz.build_finance_api.domain.user.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
