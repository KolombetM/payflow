package dev.kolombetm.payflow.account;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AccountService {
    private final Map<UUID, Account> accounts = new ConcurrentHashMap<>();

    public Account createAccount(CreateAccountRequest request) {
        var account = new Account();
        account.setId(UUID.randomUUID());
        account.setBalance(BigDecimal.ZERO);
        account.setOwnerName(request.ownerName());
        account.setCurrency(request.currency());
        accounts.put(account.getId(), account);
        return account;
    }

    public Optional<Account> findById(UUID id) {
        return Optional.ofNullable(accounts.get(id));
    }
}
