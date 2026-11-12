package dev.kolombetm.payflow.payment;

import dev.kolombetm.payflow.common.BusinessRuleException;

import java.util.UUID;

public class SameAccountTransferException extends BusinessRuleException {
    public SameAccountTransferException(UUID accountId) {
        super("Cannot transfer from account " + accountId + " to itself");
    }
}
