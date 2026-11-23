package dev.kolombetm.payflow.idempotency;

import org.springframework.data.repository.Repository;

public interface IdempotencyRepository extends Repository<IdempotencyKey, String> {
    IdempotencyKey save(IdempotencyKey key);
    IdempotencyKey findById(String id);
}
