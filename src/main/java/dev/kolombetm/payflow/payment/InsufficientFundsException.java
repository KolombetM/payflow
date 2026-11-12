package dev.kolombetm.payflow.payment;

import dev.kolombetm.payflow.common.BusinessRuleException;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

public class InsufficientFundsException extends BusinessRuleException {
    public InsufficientFundsException(UUID fromAccountId, BigDecimal amount, Currency currency) {
        super("Account: " + fromAccountId + " doesn't have enough funds: " + amount + " " + currency);
    }
}
