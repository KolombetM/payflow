package dev.kolombetm.payflow.account;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    Optional<Account> findByIdForUpdate(UUID id);

    @Query("select a.currency from Account a where a.id = :id")
    Optional<Currency> findCurrencyById(UUID id);

    @Query("select a.id from Account a where a.type = 'EXTERNAL' and a.currency = :currency")
    Optional<UUID> findExternalAccountIdByCurrency(Currency currency);
}
