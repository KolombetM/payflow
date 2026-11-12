package dev.kolombetm.payflow.common;

public abstract class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
