package dev.kolombetm.payflow.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID fromAccountId,
        UUID toAccountId,
        BigDecimal amount,
        PaymentStatus status,
        PaymentType paymentType,
        Instant createdAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(payment.getId(), payment.getFromAccountId(), payment.getToAccountId(), payment.getAmount(), payment.getStatus(), payment.getPaymentType(), payment.getCreatedAt());
    }
}
