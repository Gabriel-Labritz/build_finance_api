package com.gabriellabritz.build_finance_api.infra.exceptions.recurring_transactions;

public class RecurringTransactionAlreadyActiveException extends RuntimeException {
    public RecurringTransactionAlreadyActiveException(String message) {
        super(message);
    }
}
