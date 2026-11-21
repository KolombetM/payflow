package dev.kolombetm.payflow.ledger;

import dev.kolombetm.payflow.payment.Payment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LedgerService {
    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerService(LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(Payment payment) {
        LedgerEntry ledgerEntryFrom = LedgerEntry.createLedgerEntry(payment.getId(), payment.getFromAccountId(), payment.getAmount().negate(), payment.getCreatedAt());
        LedgerEntry ledgerEntryTo = LedgerEntry.createLedgerEntry(payment.getId(), payment.getToAccountId(), payment.getAmount(), payment.getCreatedAt());
        ledgerEntryRepository.save(ledgerEntryFrom);
        ledgerEntryRepository.save(ledgerEntryTo);
    }
}
