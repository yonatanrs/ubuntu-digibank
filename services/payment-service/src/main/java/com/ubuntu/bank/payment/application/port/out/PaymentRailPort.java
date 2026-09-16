package com.ubuntu.bank.payment.application.port.out;

import com.ubuntu.bank.payment.domain.Payment;
import java.math.BigDecimal;
import java.util.UUID;

/** Bank-owned port. Vendor-specific SDKs and payloads must not cross this boundary. */
public interface PaymentRailPort {
    String providerId();
    int priority();
    boolean supports(Payment.Rail rail);
    SubmissionOutcome submit(PaymentInstruction instruction)
        throws ProviderUnavailableException, IndeterminateOutcomeException;

    record PaymentInstruction(UUID paymentId, String debtorAccountId, String creditorAccountId,
                              BigDecimal amount, String currency, Payment.Rail rail, String reference) {}

    record SubmissionOutcome(String providerId, String externalReference, OutcomeStatus status) {}

    enum OutcomeStatus { ACCEPTED, REJECTED }

    /** Safe to try another provider: the instruction was definitely not accepted. */
    final class ProviderUnavailableException extends RuntimeException {
        public ProviderUnavailableException(String message) { super(message); }
    }

    /** Do not fail over: acceptance is unknown and must be resolved by status inquiry/reconciliation. */
    final class IndeterminateOutcomeException extends RuntimeException {
        private final String providerId;
        public IndeterminateOutcomeException(String providerId, String message) {
            super(message); this.providerId = providerId;
        }
        public String providerId() { return providerId; }
    }
}

