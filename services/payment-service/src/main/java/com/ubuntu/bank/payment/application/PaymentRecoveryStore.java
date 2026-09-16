package com.ubuntu.bank.payment.application;

import com.ubuntu.bank.payment.application.port.out.PaymentEventPort;
import com.ubuntu.bank.payment.domain.Payment;
import com.ubuntu.bank.payment.domain.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentRecoveryStore {
    private final PaymentRepository payments;
    private final PaymentEventPort events;
    private final PaymentAuditTrail audit;
    public PaymentRecoveryStore(PaymentRepository payments, PaymentEventPort events, PaymentAuditTrail audit) {
        this.payments = payments; this.events = events; this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<UUID> staleDispatchIds(Instant threshold) {
        return payments.findTop100ByStatusAndUpdatedAtLessThanEqualOrderByUpdatedAtAsc(
            Payment.Status.DISPATCHING, threshold).stream().map(Payment::getId).toList();
    }

    @Transactional(readOnly = true)
    public List<UUID> staleReconciliationIds(Instant threshold) {
        return payments.findTop100ByStatusAndUpdatedAtLessThanEqualOrderByUpdatedAtAsc(
            Payment.Status.RECONCILING, threshold).stream().map(Payment::getId).toList();
    }

    @Transactional
    public boolean recoverDispatch(UUID id, Instant threshold) {
        if (payments.recoverStaleDispatch(id, threshold) != 1) return false;
        Payment payment = payments.findById(id).orElseThrow();
        audit.record(payment, Payment.Status.DISPATCHING, "STALE_DISPATCH_RECOVERED");
        events.append(payment, "PaymentStatusChanged");
        return true;
    }

    @Transactional
    public boolean recoverReconciliation(UUID id, Instant threshold) {
        if (payments.recoverStaleReconciliation(id, threshold) != 1) return false;
        Payment payment = payments.findById(id).orElseThrow();
        audit.record(payment, Payment.Status.RECONCILING, "STALE_RECONCILIATION_RECOVERED");
        events.append(payment, "PaymentStatusChanged");
        return true;
    }
}

