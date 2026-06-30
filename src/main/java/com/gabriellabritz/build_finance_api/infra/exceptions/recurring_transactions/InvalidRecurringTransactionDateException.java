package com.gabriellabritz.build_finance_api.infra.exceptions.recurring_transactions;

public class InvalidRecurringTransactionDateException extends RuntimeException {
    public InvalidRecurringTransactionDateException(String message) {
        super(message);
    }
}
