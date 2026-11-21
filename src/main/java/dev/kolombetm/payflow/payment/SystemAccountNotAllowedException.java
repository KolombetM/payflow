package dev.kolombetm.payflow.payment;

import dev.kolombetm.payflow.common.BusinessRuleException;

public class SystemAccountNotAllowedException extends BusinessRuleException {
    public SystemAccountNotAllowedException() {
        super("One of the accounts cannot take part in this type of payment");
    }
}
