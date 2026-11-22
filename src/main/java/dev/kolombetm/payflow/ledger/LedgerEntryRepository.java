package dev.kolombetm.payflow.ledger;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends Repository<LedgerEntry, UUID> {
    LedgerEntry save(LedgerEntry entry);
    List<LedgerEntry> findByAccountId(UUID accountId);

    @Query(value = "select a.currency " +
            "from ledger_entries e join accounts a on a.id = e.account_id " +
            "group by a.currency " +
            "having sum(e.amount) <> 0;", nativeQuery = true)
    List<String> findUnbalancedCurrencies();

    @Query(value = "select a.id " +
            "from accounts a " +
            "         left join ledger_entries e on e.account_id = a.id " +
            "group by a.id, a.balance " +
            "having a.balance <> coalesce(sum(e.amount), 0);", nativeQuery = true)
    List<UUID> findAccountsWithBalanceMismatch();

    @Query(value = "select payments.id from payments " +
            "left join ledger_entries " +
            "on payments.id = ledger_entries.payment_id " +
            "where ledger_entries.payment_id is null;", nativeQuery = true)
    List<UUID> findPaymentsWithoutEntries();
}
