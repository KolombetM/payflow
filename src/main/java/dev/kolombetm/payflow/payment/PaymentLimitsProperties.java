package dev.kolombetm.payflow.payment;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.Currency;

@ConfigurationProperties(prefix = "payflow.limits")
@Validated
public record PaymentLimitsProperties(
        @NotNull @Positive BigDecimal daily,
        @NotNull @Positive BigDecimal perTransaction,
        @NotNull Currency currency
) {
    @AssertTrue(message = "per-transaction limit must not exceed daily limit")
    public boolean isPerTransactionWithinDaily() {
        return perTransaction == null || daily == null || perTransaction.compareTo(daily) <= 0;
    }
}
