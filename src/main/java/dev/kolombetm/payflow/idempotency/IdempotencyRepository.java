package dev.kolombetm.payflow.idempotency;

import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface IdempotencyRepository extends Repository<IdempotencyKey, String> {
    IdempotencyKey save(IdempotencyKey key);
    Optional<IdempotencyKey> findByKey(String key);
}
