package com.ubuntu.bank.payment.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findByClaimedByAndPublishedAtIsNullAndDeadLetteredAtIsNullOrderByCreatedAtAsc(String claimedBy);

    @Modifying
    @Query(value = """
        WITH candidates AS (
          SELECT id FROM outbox_event
          WHERE published_at IS NULL
            AND dead_lettered_at IS NULL
            AND next_attempt_at <= CURRENT_TIMESTAMP
            AND (claimed_at IS NULL OR claimed_at < CURRENT_TIMESTAMP - INTERVAL '5 minutes')
          ORDER BY created_at
          FOR UPDATE SKIP LOCKED
          LIMIT :batchSize
        )
        UPDATE outbox_event o
        SET claimed_by = :workerId, claimed_at = CURRENT_TIMESTAMP
        FROM candidates c
        WHERE o.id = c.id
        """, nativeQuery = true)
    int claimBatch(@Param("workerId") String workerId, @Param("batchSize") int batchSize);
}
