package com.ubuntu.bank.payment.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    List<Payment> findTop100ByStatusOrderByCreatedAtAsc(Payment.Status status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE payment SET status = 'DISPATCHING', updated_at = CURRENT_TIMESTAMP, version = version + 1 " +
        "WHERE id = :id AND status = 'RECEIVED'", nativeQuery = true)
    int claimForDispatch(@Param("id") UUID id);
}
