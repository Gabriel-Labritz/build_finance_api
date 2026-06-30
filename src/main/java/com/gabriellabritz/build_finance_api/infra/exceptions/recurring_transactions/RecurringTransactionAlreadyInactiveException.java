package com.gabriellabritz.build_finance_api.infra.exceptions.recurring_transactions;

public class RecurringTransactionAlreadyInactiveException extends RuntimeException {
    public RecurringTransactionAlreadyInactiveException(String message) {
        super(message);
    }
}
