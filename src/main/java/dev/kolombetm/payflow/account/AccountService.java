package dev.kolombetm.payflow.account;

import jakarta.validation.constraints.Positive;
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

    public Account getById(UUID id) {
        return findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    public Account deposit(UUID accountId, BigDecimal amount) {
        var account = getById(accountId);
        account.setBalance(account.getBalance().add(amount));
        return account;
    }

    public Account withdraw(UUID accountId, BigDecimal amount) {
        var account = getById(accountId);
        account.setBalance(account.getBalance().subtract(amount));
        return account;
    }
}
