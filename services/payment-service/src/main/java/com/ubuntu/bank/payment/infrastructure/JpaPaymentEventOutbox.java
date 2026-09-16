package com.ubuntu.bank.payment.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubuntu.bank.payment.application.port.out.PaymentEventPort;
import com.ubuntu.bank.payment.domain.Payment;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JpaPaymentEventOutbox implements PaymentEventPort {
    private final OutboxRepository outbox;
    private final ObjectMapper json;
    public JpaPaymentEventOutbox(OutboxRepository outbox, ObjectMapper json) {
        this.outbox = outbox; this.json = json;
    }

    @Override
    public void append(Payment payment, String eventType) {
        UUID eventId = UUID.randomUUID();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventId", eventId);
        payload.put("schemaVersion", 1);
        payload.put("correlationId", correlationId());
        payload.put("causationId", eventId);
        payload.put("eventType", eventType);
        payload.put("occurredAt", Instant.now());
        payload.put("paymentId", payment.getId());
        payload.put("status", payment.getStatus());
        payload.put("amount", payment.getAmount());
        payload.put("currency", payment.getCurrency());
        payload.put("rail", payment.getRail());
        payload.put("externalProvider", payment.getExternalProvider());
        payload.put("externalReference", payment.getExternalReference());
        payload.put("failureCode", payment.getFailureCode());
        payload.put("inquiryAttempts", payment.getInquiryAttempts());
        payload.put("nextInquiryAt", payment.getNextInquiryAt());
        try {
            outbox.save(new OutboxEvent(eventId, payment.getId().toString(), eventType,
                json.writeValueAsString(payload)));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize payment event", e);
        }
    }

    private String correlationId() {
        String value = MDC.get("correlationId");
        return value == null ? UUID.randomUUID().toString() : value;
    }
}
