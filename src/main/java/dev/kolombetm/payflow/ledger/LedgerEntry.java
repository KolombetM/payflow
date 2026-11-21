package dev.kolombetm.payflow.ledger;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="ledger_entries")
@Immutable
public class LedgerEntry {
    @Id
    private UUID id;
    private UUID paymentId;
    private UUID accountId;
    private BigDecimal amount;
    private Instant createdAt;

    protected LedgerEntry() {}

    public static LedgerEntry createLedgerEntry(UUID paymentId, UUID accountId, BigDecimal amount, Instant createdAt) {
        LedgerEntry ledgerEntry = new LedgerEntry();
        ledgerEntry.id = UUID.randomUUID();
        ledgerEntry.paymentId = paymentId;
        ledgerEntry.accountId = accountId;
        ledgerEntry.amount = amount;
        ledgerEntry.createdAt = createdAt;
        return ledgerEntry;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
