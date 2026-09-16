package com.ubuntu.bank.payment.application;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.time.Instant;

@Component
@ConditionalOnProperty(name = "bank.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class PaymentRecoveryWorker {
    private final PaymentRecoveryStore store;
    private final Counter dispatchRecovered;
    private final Counter reconciliationRecovered;
    public PaymentRecoveryWorker(PaymentRecoveryStore store, MeterRegistry metrics) {
        this.store = store;
        this.dispatchRecovered = metrics.counter("bank.payment.stale.dispatch.recovered");
        this.reconciliationRecovered = metrics.counter("bank.payment.stale.reconciliation.recovered");
    }

    @Scheduled(fixedDelayString = "${bank.recovery.delay-ms:60000}")
    public void recover() {
        Instant dispatchThreshold = Instant.now().minus(Duration.ofMinutes(5));
        for (var id : store.staleDispatchIds(dispatchThreshold)) {
            if (store.recoverDispatch(id, dispatchThreshold)) dispatchRecovered.increment();
        }
        Instant reconciliationThreshold = Instant.now().minus(Duration.ofMinutes(10));
        for (var id : store.staleReconciliationIds(reconciliationThreshold)) {
            if (store.recoverReconciliation(id, reconciliationThreshold)) reconciliationRecovered.increment();
        }
    }
}

