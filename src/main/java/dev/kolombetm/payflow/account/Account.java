package dev.kolombetm.payflow.account;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;
@Entity
@Table(name="accounts")
public class Account {
    @Id
    private UUID id;
    private String ownerName;
    private BigDecimal balance;
    private Currency currency;

    protected Account(){}

    public Account(String ownerName, Currency currency) {
        this.id = UUID.randomUUID();
        this.ownerName = ownerName;
        this.currency = currency;
        this.balance = BigDecimal.ZERO;
    }

    public UUID getId() {
        return id;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    protected void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Currency getCurrency() {
        return currency;
    }
}
