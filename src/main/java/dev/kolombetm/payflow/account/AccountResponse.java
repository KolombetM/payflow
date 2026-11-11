package dev.kolombetm.payflow.account;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String ownerName,
        BigDecimal balance,
        Currency currency
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(account.getId(), account.getOwnerName(), account.getBalance(), account.getCurrency());
    }
}
