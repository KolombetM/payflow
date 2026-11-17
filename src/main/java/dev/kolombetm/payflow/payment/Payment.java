package dev.kolombetm.payflow.payment;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="payments")
public class Payment {
    @Id
    private UUID id;
    private UUID fromAccountId;
    private UUID toAccountId;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    @Enumerated(EnumType.STRING)
    private PaymentType type;
    private Instant createdAt;

    protected Payment(){}

    private Payment(UUID fromAccountId, UUID toAccountId, BigDecimal amount, PaymentType paymentType, Instant createdAt) {
        this.id = UUID.randomUUID();
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.type = paymentType;
        this.status = PaymentStatus.COMPLETED;
        this.createdAt = createdAt;
    }

    public static Payment transfer(UUID fromAccountId, UUID toAccountId, BigDecimal amount, Instant createdAt) {
        return new Payment(fromAccountId, toAccountId, amount, PaymentType.TRANSFER, createdAt);
    }

    public static Payment deposit(UUID toAccountId, BigDecimal amount, Instant createdAt) {
        return new Payment(null, toAccountId, amount, PaymentType.DEPOSIT, createdAt);
    }

    public PaymentType getType() {
        return type;
    }
    public UUID getFromAccountId() {
        return fromAccountId;
    }
    public UUID getToAccountId() {
        return toAccountId;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public PaymentStatus getStatus() {
        return status;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public UUID getId() {
        return id;
    }

}
