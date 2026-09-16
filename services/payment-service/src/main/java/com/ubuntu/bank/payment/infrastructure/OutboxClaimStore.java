package com.ubuntu.bank.payment.infrastructure;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class OutboxClaimStore {
    private final OutboxRepository outbox;
    public OutboxClaimStore(OutboxRepository outbox) { this.outbox = outbox; }

    @Transactional
    public List<Envelope> claim(String workerId, int batchSize) {
        outbox.claimBatch(workerId, batchSize);
        return outbox.findByClaimedByAndPublishedAtIsNullAndDeadLetteredAtIsNullOrderByCreatedAtAsc(workerId)
            .stream().map(e -> new Envelope(e.getId(), e.getAggregateId(), e.getPayload())).toList();
    }

    public record Envelope(UUID id, String aggregateId, String payload) {}
}

