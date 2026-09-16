package com.ubuntu.bank.payment.infrastructure;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_event")
public class OutboxEvent {
    @Id private UUID id;
    @Column(nullable = false) private String aggregateId;
    @Column(nullable = false) private String eventType;
    @Column(nullable = false, columnDefinition = "text") private String payload;
    @Column(nullable = false) private Instant createdAt;
    private Instant publishedAt;
    private String claimedBy;
    private Instant claimedAt;
    @Column(nullable = false) private int publishAttempts;
    @Column(nullable = false) private Instant nextAttemptAt;
    @Column(length = 500) private String lastError;
    private Instant deadLetteredAt;

    protected OutboxEvent() {}
    public OutboxEvent(UUID id, String aggregateId, String eventType, String payload) {
        this.id = id; this.aggregateId = aggregateId; this.eventType = eventType;
        this.payload = payload; this.createdAt = Instant.now(); this.nextAttemptAt = this.createdAt;
    }
    public UUID getId() { return id; }
    public String getAggregateId() { return aggregateId; }
    public String getPayload() { return payload; }
    public String getClaimedBy() { return claimedBy; }
    public int getPublishAttempts() { return publishAttempts; }
    public Instant getDeadLetteredAt() { return deadLetteredAt; }
    public Instant getPublishedAt() { return publishedAt; }
    public void markPublished() { this.publishedAt = Instant.now(); this.claimedBy = null; this.claimedAt = null; }
    public void recordFailure(String error, int maxAttempts) {
        publishAttempts++;
        lastError = error == null ? "unknown" : error.substring(0, Math.min(error.length(), 500));
        claimedBy = null; claimedAt = null;
        if (publishAttempts >= maxAttempts) {
            deadLetteredAt = Instant.now();
        } else {
            long delaySeconds = Math.min(300, 1L << Math.min(publishAttempts, 8));
            nextAttemptAt = Instant.now().plusSeconds(delaySeconds);
        }
    }
}
