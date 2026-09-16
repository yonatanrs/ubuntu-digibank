package com.ubuntu.bank.payment.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    List<Payment> findTop100ByStatusOrderByCreatedAtAsc(Payment.Status status);
    Page<Payment> findByStatusOrderByUpdatedAtAsc(Payment.Status status, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE payment SET status = 'DISPATCHING', updated_at = CURRENT_TIMESTAMP, version = version + 1 " +
        "WHERE id = :id AND status = 'RECEIVED'", nativeQuery = true)
    int claimForDispatch(@Param("id") UUID id);

    List<Payment> findTop100ByStatusAndNextInquiryAtLessThanEqualOrderByUpdatedAtAsc(
        Payment.Status status, Instant now);

    List<Payment> findTop100ByStatusAndUpdatedAtLessThanEqualOrderByUpdatedAtAsc(
        Payment.Status status, Instant threshold);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE payment SET status = 'RECONCILING', updated_at = CURRENT_TIMESTAMP, version = version + 1 " +
        "WHERE id = :id AND status = 'PENDING_CONFIRMATION' AND next_inquiry_at <= CURRENT_TIMESTAMP",
        nativeQuery = true)
    int claimForReconciliation(@Param("id") UUID id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE payment SET status = 'PENDING_CONFIRMATION', " +
        "external_provider = COALESCE(external_provider, 'UNKNOWN_PROVIDER'), " +
        "failure_code = 'STALE_DISPATCH_CLAIM', next_inquiry_at = CURRENT_TIMESTAMP, " +
        "updated_at = CURRENT_TIMESTAMP, version = version + 1 " +
        "WHERE id = :id AND status = 'DISPATCHING' AND updated_at <= :threshold", nativeQuery = true)
    int recoverStaleDispatch(@Param("id") UUID id, @Param("threshold") Instant threshold);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE payment SET status = 'PENDING_CONFIRMATION', " +
        "failure_code = 'STALE_RECONCILIATION_CLAIM', next_inquiry_at = CURRENT_TIMESTAMP, " +
        "updated_at = CURRENT_TIMESTAMP, version = version + 1 " +
        "WHERE id = :id AND status = 'RECONCILING' AND updated_at <= :threshold", nativeQuery = true)
    int recoverStaleReconciliation(@Param("id") UUID id, @Param("threshold") Instant threshold);
}
