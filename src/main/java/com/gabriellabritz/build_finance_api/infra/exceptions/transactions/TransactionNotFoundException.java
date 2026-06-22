package com.gabriellabritz.build_finance_api.infra.exceptions.transactions;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(String message) {
        super(message);
    }
}
