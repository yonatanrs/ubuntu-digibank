package com.ubuntu.bank.payment.application.port.out;

import java.math.BigDecimal;
import java.util.UUID;
import com.ubuntu.bank.payment.domain.Payment;

/** Provider inquiry port used only to resolve an already-submitted or uncertain payment. */
public interface PaymentStatusInquiryPort {
    String providerId();
    boolean supports(Payment.Rail rail);
    InquiryOutcome inquire(InquiryRequest request) throws InquiryUnavailableException;

    record InquiryRequest(UUID paymentId, String providerId, String externalReference,
                          BigDecimal amount, String currency, Payment.Rail rail) {}

    record InquiryOutcome(OutcomeStatus status, String externalReference, String reasonCode) {}

    enum OutcomeStatus { ACCEPTED, REJECTED, NOT_FOUND_FINAL, UNKNOWN }

    final class InquiryUnavailableException extends RuntimeException {
        public InquiryUnavailableException(String message) { super(message); }
    }
}
