package com.ubuntu.bank.payment;

import com.ubuntu.bank.payment.infrastructure.OutboxEvent;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class OutboxEventTest {
    @Test
    void deadLettersAfterMaximumAttemptsWithoutThrowingAwayPayload() {
        OutboxEvent event = new OutboxEvent(UUID.randomUUID(), "payment-1", "PaymentInitiated", "{\"safe\":true}");
        event.recordFailure("broker unavailable", 2);
        assertThat(event.getDeadLetteredAt()).isNull();
        event.recordFailure("broker unavailable", 2);
        assertThat(event.getPublishAttempts()).isEqualTo(2);
        assertThat(event.getDeadLetteredAt()).isNotNull();
        assertThat(event.getPayload()).contains("safe");
    }
}
