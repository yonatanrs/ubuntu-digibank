package com.ubuntu.bank.payment.application;

import com.ubuntu.bank.payment.application.port.out.PaymentRailPort;
import com.ubuntu.bank.payment.application.port.out.PaymentRailPort.*;
import com.ubuntu.bank.payment.domain.Payment;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class PaymentRailRouterTest {
    private final PaymentInstruction instruction = new PaymentInstruction(UUID.randomUUID(), "ZA-1", "ZA-2",
        new BigDecimal("125.50"), "ZAR", Payment.Rail.PAYSHAP, "Invoice 42");

    @Test
    void fallsBackOnlyWhenPrimaryDefinitelyDidNotAcceptPayment() {
        PaymentRailPort primary = provider("primary", 1, new ProviderUnavailableException("connection refused"));
        PaymentRailPort secondary = provider("secondary", 2,
            new SubmissionOutcome("secondary", "EXT-2", OutcomeStatus.ACCEPTED));

        SubmissionOutcome result = new PaymentRailRouter(List.of(secondary, primary)).submit(instruction);
        assertThat(result.providerId()).isEqualTo("secondary");
    }

    @Test
    void neverFailsOverWhenPrimaryOutcomeIsUnknown() {
        PaymentRailPort primary = provider("primary", 1,
            new IndeterminateOutcomeException("primary", "timeout after request was sent"));
        PaymentRailPort secondary = provider("secondary", 2,
            new SubmissionOutcome("secondary", "EXT-2", OutcomeStatus.ACCEPTED));

        assertThatThrownBy(() -> new PaymentRailRouter(List.of(primary, secondary)).submit(instruction))
            .isInstanceOf(IndeterminateOutcomeException.class);
    }

    private PaymentRailPort provider(String id, int priority, Object result) {
        return new PaymentRailPort() {
            public String providerId() { return id; }
            public int priority() { return priority; }
            public boolean supports(Payment.Rail rail) { return rail == Payment.Rail.PAYSHAP; }
            public SubmissionOutcome submit(PaymentInstruction ignored) {
                if (result instanceof RuntimeException failure) throw failure;
                return (SubmissionOutcome) result;
            }
        };
    }
}

