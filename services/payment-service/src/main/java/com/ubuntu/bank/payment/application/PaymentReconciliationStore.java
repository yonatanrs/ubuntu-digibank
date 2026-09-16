package com.ubuntu.bank.payment.application;

import com.ubuntu.bank.payment.application.port.out.PaymentEventPort;
import com.ubuntu.bank.payment.application.port.out.PaymentStatusInquiryPort.InquiryOutcome;
import com.ubuntu.bank.payment.application.port.out.PaymentStatusInquiryPort.InquiryRequest;
import com.ubuntu.bank.payment.domain.Payment;
import com.ubuntu.bank.payment.domain.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentReconciliationStore {
    private final PaymentRepository payments;
    private final PaymentEventPort events;
    private final PaymentAuditTrail audit;

    public PaymentReconciliationStore(PaymentRepository payments, PaymentEventPort events,
                                      PaymentAuditTrail audit) {
        this.payments = payments; this.events = events; this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<UUID> dueIds(Instant now) {
        return payments.findTop100ByStatusAndNextInquiryAtLessThanEqualOrderByUpdatedAtAsc(
            Payment.Status.PENDING_CONFIRMATION, now).stream().map(Payment::getId).toList();
    }

    @Transactional
    public Optional<InquiryRequest> claim(UUID id) {
        if (payments.claimForReconciliation(id) != 1) return Optional.empty();
        Payment payment = payments.findById(id).orElseThrow();
        audit.record(payment, Payment.Status.PENDING_CONFIRMATION, "RECONCILIATION_CLAIMED");
        return Optional.of(new InquiryRequest(payment.getId(), payment.getExternalProvider(),
            payment.getExternalReference(), payment.getAmount(), payment.getCurrency(), payment.getRail()));
    }

    @Transactional
    public void resolve(UUID id, InquiryOutcome outcome) {
        Payment payment = payments.findById(id).orElseThrow();
        Payment.Status before = payment.getStatus();
        switch (outcome.status()) {
            case ACCEPTED -> payment.resolveReconciliationAsSubmitted(outcome.externalReference());
            case REJECTED, NOT_FOUND_FINAL -> payment.resolveReconciliationAsRejected(
                outcome.externalReference(), outcome.reasonCode() == null ? "PROVIDER_REJECTED" : outcome.reasonCode());
            case UNKNOWN -> throw new IllegalArgumentException("UNKNOWN outcome must be rescheduled");
        }
        audit.record(payment, before, "RECONCILIATION_" + outcome.status().name());
        events.append(payment, "PaymentStatusChanged");
    }

    @Transactional
    public Payment.Status reschedule(UUID id, String reasonCode, int maxAttempts) {
        Payment payment = payments.findById(id).orElseThrow();
        Payment.Status before = payment.getStatus();
        payment.rescheduleReconciliation(reasonCode, maxAttempts);
        audit.record(payment, before, payment.getStatus() == Payment.Status.MANUAL_REVIEW_REQUIRED
            ? "RECONCILIATION_ESCALATED" : "RECONCILIATION_RETRY_SCHEDULED");
        events.append(payment, "PaymentStatusChanged");
        return payment.getStatus();
    }
}
