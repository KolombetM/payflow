package dev.kolombetm.payflow.payment;

import dev.kolombetm.payflow.common.BusinessRuleException;

import java.util.Currency;
import java.util.UUID;

public class DifferentCurrencyException extends BusinessRuleException {
    public DifferentCurrencyException(UUID fromAccountId, Currency fromCurrency, UUID toAccountId, Currency toCurrency) {
        super("Currency mismatch between account: " +  fromAccountId + " that is in " + fromCurrency + " and " + toAccountId + " that is in " + toCurrency);
    }
}