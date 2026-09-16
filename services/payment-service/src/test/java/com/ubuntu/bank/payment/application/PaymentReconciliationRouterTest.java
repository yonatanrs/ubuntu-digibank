package com.ubuntu.bank.payment.application;

import com.ubuntu.bank.payment.application.port.out.PaymentStatusInquiryPort;
import com.ubuntu.bank.payment.application.port.out.PaymentStatusInquiryPort.*;
import com.ubuntu.bank.payment.domain.Payment;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class PaymentReconciliationRouterTest {
    private final InquiryRequest knownRequest = new InquiryRequest(UUID.randomUUID(), "provider-a", null,
        new BigDecimal("125.50"), "ZAR", Payment.Rail.PAYSHAP);

    @Test
    void routesKnownPaymentOnlyToItsOriginalProvider() {
        PaymentReconciliationRouter router = new PaymentReconciliationRouter(List.of(
            provider("provider-b", OutcomeStatus.UNKNOWN), provider("provider-a", OutcomeStatus.ACCEPTED)));
        assertThat(router.inquire(knownRequest).status()).isEqualTo(OutcomeStatus.ACCEPTED);
    }

    @Test
    void unknownProviderSearchesEligibleProvidersWithoutResubmittingPayment() {
        InquiryRequest unknown = new InquiryRequest(UUID.randomUUID(), "UNKNOWN_PROVIDER", null,
            new BigDecimal("50.00"), "ZAR", Payment.Rail.PAYSHAP);
        PaymentReconciliationRouter router = new PaymentReconciliationRouter(List.of(
            provider("provider-a", OutcomeStatus.UNKNOWN), provider("provider-b", OutcomeStatus.ACCEPTED)));
        assertThat(router.inquire(unknown).status()).isEqualTo(OutcomeStatus.ACCEPTED);
    }

    @Test
    void missingProviderFailsClosed() {
        PaymentReconciliationRouter router = new PaymentReconciliationRouter(List.of());
        assertThatThrownBy(() -> router.inquire(knownRequest))
            .isInstanceOf(PaymentReconciliationRouter.InquiryProviderNotConfiguredException.class);
    }

    private PaymentStatusInquiryPort provider(String id, OutcomeStatus status) {
        return new PaymentStatusInquiryPort() {
            public String providerId() { return id; }
            public boolean supports(Payment.Rail rail) { return rail == Payment.Rail.PAYSHAP; }
            public InquiryOutcome inquire(InquiryRequest request) {
                return new InquiryOutcome(status, status == OutcomeStatus.ACCEPTED ? "EXT-1" : null, null);
            }
        };
    }
}
