package com.ubuntu.bank.payment;

import com.ubuntu.bank.payment.domain.Payment;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentDomainTest {
    @Test
    void createsReceivedZarPaymentWithoutUsingFloatingPointMoney() {
        Payment payment = Payment.receive("idem-0001", "fingerprint", "ZA-1", "ZA-2",
            new BigDecimal("125.50"), "ZAR", Payment.Rail.PAYSHAP, "Invoice 42");
        assertThat(payment.getStatus()).isEqualTo(Payment.Status.RECEIVED);
        assertThat(payment.getAmount()).isEqualByComparingTo("125.50");
    }

    @Test
    void forbidsSkippingTheDispatchClaimState() {
        Payment payment = Payment.receive("idem-0002", "fingerprint", "ZA-1", "ZA-2",
            new BigDecimal("50.00"), "ZAR", Payment.Rail.PAYSHAP, "Invoice 43");
        assertThatThrownBy(() -> payment.markSubmitted("provider", "EXT-1"))
            .isInstanceOf(IllegalStateException.class);
    }
}
