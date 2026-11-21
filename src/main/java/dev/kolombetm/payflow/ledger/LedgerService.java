package dev.kolombetm.payflow.ledger;

import dev.kolombetm.payflow.payment.Payment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class LedgerService {
    private final Clock clock;
    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerService(Clock clock, LedgerEntryRepository ledgerEntryRepository) {
        this.clock = clock;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public void createLedgerEntry(Payment payment) {
        LedgerEntry ledgerEntryFrom = LedgerEntry.createLedgerEntry(payment.getId(), payment.getFromAccountId(), payment.getAmount().negate(), Instant.now(clock));
        LedgerEntry ledgerEntryTo = LedgerEntry.createLedgerEntry(payment.getId(), payment.getToAccountId(), payment.getAmount(), Instant.now(clock));
        ledgerEntryFrom = ledgerEntryRepository.save(ledgerEntryFrom);
        ledgerEntryTo = ledgerEntryRepository.save(ledgerEntryTo);
    }
}
