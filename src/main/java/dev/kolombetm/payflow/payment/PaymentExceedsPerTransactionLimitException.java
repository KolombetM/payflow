package dev.kolombetm.payflow.payment;

import dev.kolombetm.payflow.common.BusinessRuleException;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentExceedsPerTransactionLimitException extends BusinessRuleException {
    public PaymentExceedsPerTransactionLimitException(UUID  fromAccountId, UUID toAccountId, BigDecimal amount, BigDecimal paymentLimit) {
        super("Payment from account: " + fromAccountId + " to account: " + toAccountId + " in amount of: " + amount + " exceeds the daily limit: " + paymentLimit);
    }
}
