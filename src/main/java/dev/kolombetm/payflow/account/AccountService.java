package dev.kolombetm.payflow.account;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Account createAccount(CreateAccountRequest request) {
        var account = new Account(request.ownerName(), request.currency());
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public Optional<Account> findById(UUID id) {
        return accountRepository.findById(id);
    }

    public Account getById(UUID id) {
        return findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Account getByIdForUpdate(UUID id) {
        return accountRepository.findByIdForUpdate(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Transactional
    public Account deposit(UUID accountId, BigDecimal amount) {
        var account = getByIdForUpdate(accountId);
        account.setBalance(account.getBalance().add(amount));
        return account;
    }

    @Transactional
    public Account withdraw(UUID accountId, BigDecimal amount) {
        var account = getByIdForUpdate(accountId);
        if (account.getType().equals(AccountType.CUSTOMER) && account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(accountId, amount, account.getCurrency());
        }
        account.setBalance(account.getBalance().subtract(amount));
        return account;
    }
}
