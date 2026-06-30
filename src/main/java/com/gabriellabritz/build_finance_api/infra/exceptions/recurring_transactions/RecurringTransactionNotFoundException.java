package com.gabriellabritz.build_finance_api.infra.exceptions.recurring_transactions;

public class RecurringTransactionNotFoundException extends RuntimeException {
    public RecurringTransactionNotFoundException(String message) {
        super(message);
    }
}
