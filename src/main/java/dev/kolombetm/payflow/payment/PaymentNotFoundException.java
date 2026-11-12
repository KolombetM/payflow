package dev.kolombetm.payflow.payment;

import dev.kolombetm.payflow.common.NotFoundException;

import java.util.UUID;

public class PaymentNotFoundException extends NotFoundException {
    public PaymentNotFoundException(UUID id) {
        super("Payment " + id + " not found");
    }
}
