package dev.kolombetm.payflow.payment;

import dev.kolombetm.payflow.common.BusinessRuleException;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

public class DailyLimitExceededException extends BusinessRuleException {

    public DailyLimitExceededException(UUID accountId, Currency currency, BigDecimal spentToday, BigDecimal amount, BigDecimal dailyLimit) {
        super("Failed to process transaction. Daily limit for account: " + accountId + " exceeded. Spent today: "
                + spentToday + currency + " transaction amount: " + amount + " daily limit: " + dailyLimit);
    }
}
