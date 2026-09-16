package com.ubuntu.bank.payment.application;

import com.ubuntu.bank.payment.domain.Payment;
import com.ubuntu.bank.payment.domain.audit.PaymentStatusHistory;
import com.ubuntu.bank.payment.domain.audit.PaymentStatusHistoryRepository;
import org.springframework.stereotype.Component;

@Component
public class PaymentAuditTrail {
    private final PaymentStatusHistoryRepository history;
    public PaymentAuditTrail(PaymentStatusHistoryRepository history) { this.history = history; }

    public void record(Payment payment, Payment.Status from, String reason) {
        history.save(new PaymentStatusHistory(payment.getId(), from, payment.getStatus(), reason,
            payment.getExternalProvider()));
    }
}

