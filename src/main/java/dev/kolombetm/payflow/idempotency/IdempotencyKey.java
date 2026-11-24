package dev.kolombetm.payflow.idempotency;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="idempotency_keys")
public class IdempotencyKey {
    @Id
    @Size(max = 100)
    private String key;
    private UUID paymentId;
    private Instant createdAt;

    protected IdempotencyKey() {}
    public IdempotencyKey(String key, UUID paymentId, Instant createdAt) {
        this.key = key;
        this.paymentId = paymentId;
        this.createdAt = createdAt;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public String getKey() {
        return key;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
