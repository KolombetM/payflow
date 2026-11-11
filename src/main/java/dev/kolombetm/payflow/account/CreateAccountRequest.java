package dev.kolombetm.payflow.account;

import java.util.Currency;

public record CreateAccountRequest(
        String ownerName,
        Currency currency
) {
}
