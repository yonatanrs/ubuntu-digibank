package com.ubuntu.bank.payment.infrastructure;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class OutboxDeliveryStore {
    private final OutboxRepository outbox;
    public OutboxDeliveryStore(OutboxRepository outbox) { this.outbox = outbox; }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void published(UUID eventId, String workerId) {
        OutboxEvent event = owned(eventId, workerId);
        event.markPublished();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failed(UUID eventId, String workerId, Throwable failure, int maxAttempts) {
        OutboxEvent event = owned(eventId, workerId);
        event.recordFailure(failure.getClass().getSimpleName() + ": " + failure.getMessage(), maxAttempts);
    }

    private OutboxEvent owned(UUID eventId, String workerId) {
        OutboxEvent event = outbox.findById(eventId).orElseThrow();
        if (!workerId.equals(event.getClaimedBy())) {
            throw new IllegalStateException("Outbox lease is not owned by this worker");
        }
        return event;
    }
}

