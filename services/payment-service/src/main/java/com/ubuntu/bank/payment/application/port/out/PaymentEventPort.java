package com.ubuntu.bank.payment.application.port.out;

import com.ubuntu.bank.payment.domain.Payment;

public interface PaymentEventPort {
    void append(Payment payment, String eventType);
}

