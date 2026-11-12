package dev.kolombetm.payflow.account;

import dev.kolombetm.payflow.common.NotFoundException;

import java.util.UUID;

public class AccountNotFoundException extends NotFoundException {
    public AccountNotFoundException(UUID id) {
        super("Account " + id + " not found");
    }
}
