package dev.kolombetm.payflow.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @Query("""
            select coalesce(sum(p.amount), 0) from Payment p
            where p.fromAccountId = :accountId
            and p.type = :type
            and p.status = :status
            and p.createdAt >= :since
            """)
    BigDecimal sumSpentSince(UUID accountId, PaymentType type, PaymentStatus status, Instant since);
}
