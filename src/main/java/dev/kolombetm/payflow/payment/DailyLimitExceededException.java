package dev.kolombetm.payflow.payment;

import dev.kolombetm.payflow.common.BusinessRuleException;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

public class DailyLimitExceededException extends BusinessRuleException {

    public DailyLimitExceededException(UUID accountId, Currency currency, BigDecimal spentToday, BigDecimal amount) {
        super("Failed to process transaction. Daily limit for account: " + accountId + " excided. Spent today: "
                + spentToday + currency + " transaction amount: " + amount);
    }
}
