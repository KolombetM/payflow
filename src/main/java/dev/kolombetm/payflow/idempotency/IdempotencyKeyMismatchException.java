package dev.kolombetm.payflow.idempotency;

import dev.kolombetm.payflow.common.BusinessRuleException;

import java.math.BigDecimal;
import java.util.UUID;

public class IdempotencyKeyMismatchException extends BusinessRuleException {
    public IdempotencyKeyMismatchException(UUID fromAccount, UUID toAccount, BigDecimal amount) {
        super("Payment from: " + fromAccount + " to: " + toAccount + " amount: " + amount + "already exists");
    }
}
