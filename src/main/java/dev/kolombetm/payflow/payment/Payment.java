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

    public static Payment create(UUID fromAccountId, UUID toAccountId, BigDecimal amount, PaymentType type, Instant createdAt) {
        return new Payment(fromAccountId, toAccountId, amount, type, createdAt);
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
