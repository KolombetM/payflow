package dev.kolombetm.payflow.ledger;

import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends Repository<LedgerEntry, UUID> {
    LedgerEntry save(LedgerEntry entry);
    List<LedgerEntry> findByAccountId(UUID accountId);
}
