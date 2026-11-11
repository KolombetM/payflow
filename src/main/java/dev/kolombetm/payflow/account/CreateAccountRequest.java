package dev.kolombetm.payflow.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.Currency;

public record CreateAccountRequest(
        @NotBlank String ownerName,
        @NotNull Currency currency
) {
}
