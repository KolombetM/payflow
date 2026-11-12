package dev.kolombetm.payflow.payment;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentRequest(UUID fromAccountId, UUID toAccountId, BigDecimal amount){

}
