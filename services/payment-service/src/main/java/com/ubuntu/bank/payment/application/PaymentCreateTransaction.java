package com.ubuntu.bank.payment.application;

import com.ubuntu.bank.payment.domain.Payment;
import com.ubuntu.bank.payment.domain.PaymentRepository;
import com.ubuntu.bank.payment.application.port.out.PaymentEventPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentCreateTransaction {
    private final PaymentRepository payments;
    private final PaymentEventPort outbox;
    private final PaymentAuditTrail audit;

    public PaymentCreateTransaction(PaymentRepository payments, PaymentEventPort outbox,
                                    PaymentAuditTrail audit) {
        this.payments = payments; this.outbox = outbox; this.audit = audit;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment create(String key, String fingerprint, CreatePaymentCommand r) {
        Payment payment = payments.saveAndFlush(Payment.receive(key, fingerprint, r.debtorAccountId(),
            r.creditorAccountId(), r.amount(), r.currency(), r.rail(), r.reference()));
        audit.record(payment, null, "PAYMENT_RECEIVED");
        outbox.append(payment, "PaymentInitiated");
        return payment;
    }
}
