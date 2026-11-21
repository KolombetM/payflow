package dev.kolombetm.payflow.payment;

import dev.kolombetm.payflow.common.BusinessRuleException;

public class SystemAccountNotAllowedException extends BusinessRuleException {
    public SystemAccountNotAllowedException() {
        super("Some of the specified account is not accepts this type of transfers");
    }
}
