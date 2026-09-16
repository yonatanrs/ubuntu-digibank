package com.ubuntu.bank.payment.application;

import com.ubuntu.bank.payment.application.port.out.PaymentRailPort.PaymentInstruction;
import com.ubuntu.bank.payment.application.port.out.PaymentRailPort.SubmissionOutcome;
import com.ubuntu.bank.payment.domain.Payment;
import com.ubuntu.bank.payment.domain.PaymentRepository;
import com.ubuntu.bank.payment.application.port.out.PaymentEventPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentDispatchStore {
    private final PaymentRepository payments;
    private final PaymentEventPort outbox;
    private final PaymentAuditTrail audit;
    public PaymentDispatchStore(PaymentRepository payments, PaymentEventPort outbox, PaymentAuditTrail audit) {
        this.payments = payments; this.outbox = outbox; this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<UUID> pendingIds() {
        return payments.findTop100ByStatusOrderByCreatedAtAsc(Payment.Status.RECEIVED)
            .stream().map(Payment::getId).toList();
    }

    @Transactional
    public Optional<PaymentInstruction> claim(UUID id) {
        if (payments.claimForDispatch(id) != 1) return Optional.empty();
        Payment p = payments.findById(id).orElseThrow();
        audit.record(p, Payment.Status.RECEIVED, "DISPATCH_CLAIMED");
        return Optional.of(new PaymentInstruction(p.getId(), p.getDebtorAccountId(), p.getCreditorAccountId(),
            p.getAmount(), p.getCurrency(), p.getRail(), p.getReference()));
    }

    @Transactional
    public void submitted(UUID id, SubmissionOutcome outcome) {
        Payment p = payments.findById(id).orElseThrow();
        Payment.Status before = p.getStatus();
        if (outcome.status() == com.ubuntu.bank.payment.application.port.out.PaymentRailPort.OutcomeStatus.ACCEPTED) {
            p.markSubmitted(outcome.providerId(), outcome.externalReference());
        } else {
            p.markRejected(outcome.providerId(), outcome.externalReference(), "PROVIDER_REJECTED");
        }
        audit.record(p, before, outcome.status().name());
        outbox.append(p, "PaymentStatusChanged");
    }

    @Transactional
    public void pendingConfirmation(UUID id, String providerId) {
        Payment p = payments.findById(id).orElseThrow();
        Payment.Status before = p.getStatus();
        p.markPendingConfirmation(providerId, "OUTCOME_UNKNOWN");
        audit.record(p, before, "PROVIDER_OUTCOME_UNKNOWN");
        outbox.append(p, "PaymentStatusChanged");
    }

    @Transactional
    public void failed(UUID id, String code) {
        Payment p = payments.findById(id).orElseThrow();
        Payment.Status before = p.getStatus();
        p.markFailed(code);
        audit.record(p, before, code);
        outbox.append(p, "PaymentStatusChanged");
    }
}
