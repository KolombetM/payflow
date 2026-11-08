package dev.kolombetm.payflow.payment;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "payflow.limits")
public record PaymentLimitsProperties(
        BigDecimal daily,
        BigDecimal perTransaction,
        String currency
) {
}
