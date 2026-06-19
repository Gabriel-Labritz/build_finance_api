package com.gabriellabritz.build_finance_api.infra.exceptions.transactions;

public class TransactionTypeMismatchException extends RuntimeException {
    public TransactionTypeMismatchException(String message) {
        super(message);
    }
}
