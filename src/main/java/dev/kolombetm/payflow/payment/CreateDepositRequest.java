package dev.kolombetm.payflow.payment;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateDepositRequest(
        @NotNull UUID accountId,
        @NotNull @DecimalMin("0.01") @Digits(integer = 12, fraction = 2) BigDecimal amount
) {
}
