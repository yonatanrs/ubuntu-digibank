package com.ubuntu.bank.payment.infrastructure;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "bank.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxRelay {
    private final OutboxClaimStore claims;
    private final OutboxDeliveryStore deliveries;
    private final KafkaTemplate<String, String> kafka;
    private final String workerId = UUID.randomUUID().toString();
    private final int batchSize;
    private final int maxAttempts;
    public OutboxRelay(OutboxClaimStore claims, OutboxDeliveryStore deliveries,
                       KafkaTemplate<String, String> kafka,
                       @Value("${bank.outbox.batch-size:20}") int batchSize,
                       @Value("${bank.outbox.max-attempts:10}") int maxAttempts) {
        this.claims = claims; this.deliveries = deliveries; this.kafka = kafka;
        this.batchSize = batchSize; this.maxAttempts = maxAttempts;
    }

    @Scheduled(fixedDelayString = "${bank.outbox.delay-ms:1000}")
    public void publish() {
        for (OutboxClaimStore.Envelope event : claims.claim(workerId, batchSize)) {
            try {
                kafka.send("bank.payment.events.v1", event.aggregateId(), event.payload()).join();
                deliveries.published(event.id(), workerId);
            } catch (RuntimeException failure) {
                deliveries.failed(event.id(), workerId, failure, maxAttempts);
            }
        }
    }
}
