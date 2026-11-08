package dev.kolombetm.payflow.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "payflow.limits")
@Validated
public record PaymentLimitsProperties(
        @NotNull @Positive BigDecimal daily,
        @NotNull @Positive BigDecimal perTransaction,
        @NotBlank String currency
) {
}
