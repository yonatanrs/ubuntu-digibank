package com.ubuntu.bank.payment.application;

import com.ubuntu.bank.payment.application.port.out.PaymentStatusInquiryPort.InquiryUnavailableException;
import com.ubuntu.bank.payment.application.port.out.PaymentStatusInquiryPort.OutcomeStatus;
import com.ubuntu.bank.payment.domain.Payment;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "bank.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class PaymentReconciliationWorker {
    private static final Logger log = LoggerFactory.getLogger(PaymentReconciliationWorker.class);
    private final PaymentReconciliationStore store;
    private final PaymentReconciliationRouter router;
    private final int maxAttempts;
    private final Counter resolved;
    private final Counter retried;
    private final Counter escalated;

    public PaymentReconciliationWorker(PaymentReconciliationStore store,
                                       PaymentReconciliationRouter router,
                                       MeterRegistry metrics,
                                       @Value("${bank.reconciliation.max-attempts:8}") int maxAttempts) {
        this.store = store; this.router = router; this.maxAttempts = maxAttempts;
        this.resolved = metrics.counter("bank.payment.reconciliation.resolved");
        this.retried = metrics.counter("bank.payment.reconciliation.retried");
        this.escalated = metrics.counter("bank.payment.reconciliation.escalated");
    }

    @Scheduled(fixedDelayString = "${bank.reconciliation.delay-ms:5000}")
    public void reconcile() {
        for (UUID id : store.dueIds(Instant.now())) {
            store.claim(id).ifPresent(request -> reconcileClaimed(id, request));
        }
    }

    private void reconcileClaimed(UUID id,
        com.ubuntu.bank.payment.application.port.out.PaymentStatusInquiryPort.InquiryRequest request) {
        try {
            var outcome = router.inquire(request);
            if (outcome.status() == OutcomeStatus.UNKNOWN) {
                reschedule(id, outcome.reasonCode() == null ? "PROVIDER_STILL_UNKNOWN" : outcome.reasonCode());
            } else {
                store.resolve(id, outcome); resolved.increment();
            }
        } catch (InquiryUnavailableException unavailable) {
            reschedule(id, "INQUIRY_UNAVAILABLE");
        } catch (PaymentReconciliationRouter.InquiryProviderNotConfiguredException missingAdapter) {
            reschedule(id, "INQUIRY_ADAPTER_NOT_CONFIGURED");
        } catch (RuntimeException unclassified) {
            log.error("Unclassified reconciliation failure; payment remains controlled. paymentId={}", id, unclassified);
            reschedule(id, "INQUIRY_UNCLASSIFIED_FAILURE");
        }
    }

    private void reschedule(UUID id, String reason) {
        Payment.Status status = store.reschedule(id, reason, maxAttempts);
        if (status == Payment.Status.MANUAL_REVIEW_REQUIRED) escalated.increment(); else retried.increment();
    }
}

