package dev.kolombetm.payflow.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Currency;

public record CreateAccountRequest(
        @NotBlank @Size(max = 100) String ownerName,
        @NotNull Currency currency
) {
}
