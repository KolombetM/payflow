package dev.kolombetm.payflow.ledger;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ReconciliationService {

    private final LedgerEntryRepository ledgerEntryRepository;

    ReconciliationService(LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public ReconciliationReport check() {
        List<String> unbalancedCurrencies = ledgerEntryRepository.findUnbalancedCurrencies();
        List<UUID> mismatchedAccounts = ledgerEntryRepository.findAccountsWithBalanceMismatch();
        List<UUID> paymentsWithoutEntries = ledgerEntryRepository.findPaymentsWithoutEntries();
        return new ReconciliationReport(unbalancedCurrencies, mismatchedAccounts, paymentsWithoutEntries);
    }
}
