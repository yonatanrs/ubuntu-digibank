package com.ubuntu.bank.payment.application;

import com.ubuntu.bank.payment.application.port.out.PaymentRailPort.IndeterminateOutcomeException;
import com.ubuntu.bank.payment.application.PaymentRailRouter.AllRailProvidersUnavailableException;
import com.ubuntu.bank.payment.application.PaymentRailRouter.NoRailProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Component
@ConditionalOnProperty(name = "bank.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class PaymentDispatchWorker {
    private static final Logger log = LoggerFactory.getLogger(PaymentDispatchWorker.class);
    private final PaymentDispatchStore store;
    private final PaymentRailRouter router;

    public PaymentDispatchWorker(PaymentDispatchStore store, PaymentRailRouter router) {
        this.store = store; this.router = router;
    }

    @Scheduled(fixedDelayString = "${bank.payment-dispatch.delay-ms:500}")
    public void dispatch() {
        for (var id : store.pendingIds()) {
            store.claim(id).ifPresent(instruction -> dispatchClaimed(id, instruction));
        }
    }

    private void dispatchClaimed(java.util.UUID id,
                                 com.ubuntu.bank.payment.application.port.out.PaymentRailPort.PaymentInstruction instruction) {
        try {
            var outcome = router.submit(instruction);
            try {
                store.submitted(id, outcome);
            } catch (RuntimeException persistenceFailure) {
                log.error("Provider responded but payment outcome persistence failed; reconciliation required. paymentId={}",
                    id, persistenceFailure);
            }
        } catch (IndeterminateOutcomeException unknown) {
            store.pendingConfirmation(id, unknown.providerId());
        } catch (NoRailProviderException configurationFailure) {
            store.failed(id, "RAIL_NOT_CONFIGURED");
        } catch (AllRailProvidersUnavailableException unavailable) {
            store.failed(id, "RAIL_UNAVAILABLE");
        } catch (RuntimeException unclassified) {
            log.error("Unclassified provider failure; treating outcome as unknown. paymentId={}", id, unclassified);
            store.pendingConfirmation(id, "UNKNOWN_PROVIDER");
        }
    }
}
