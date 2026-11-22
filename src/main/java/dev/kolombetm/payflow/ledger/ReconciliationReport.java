package dev.kolombetm.payflow.ledger;

import java.util.List;
import java.util.UUID;

public record ReconciliationReport(List<String> unbalancedCurrencies, List<UUID> mismatchedAccounts, List<UUID> paymentsWithoutEntries){
    public boolean isConsistent() {
        return unbalancedCurrencies().isEmpty() && mismatchedAccounts().isEmpty() && paymentsWithoutEntries().isEmpty();
    };
};
