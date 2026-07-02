package com.gabriellabritz.build_finance_api.domain.recurring_transactions.scheduler;

import com.gabriellabritz.build_finance_api.domain.recurring_transactions.RecurringTransactionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RecurringTransactionScheduler {
    private final RecurringTransactionService recurringTransactionService;

    public RecurringTransactionScheduler(RecurringTransactionService recurringTransactionService) {
        this.recurringTransactionService = recurringTransactionService;
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void processRecurringTransactions() {
        recurringTransactionService.processRecurringTransaction();
    }
}
