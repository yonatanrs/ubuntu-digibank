package com.ubuntu.bank.payment.application;

import com.ubuntu.bank.payment.application.port.out.PaymentStatusInquiryPort;
import com.ubuntu.bank.payment.application.port.out.PaymentStatusInquiryPort.InquiryRequest;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PaymentReconciliationRouter {
    private final List<PaymentStatusInquiryPort> providers;
    public PaymentReconciliationRouter(List<PaymentStatusInquiryPort> providers) {
        this.providers = List.copyOf(providers);
    }

    public PaymentStatusInquiryPort.InquiryOutcome inquire(InquiryRequest request) {
        if (!"UNKNOWN_PROVIDER".equals(request.providerId())) {
            return providers.stream()
                .filter(provider -> provider.providerId().equals(request.providerId()) && provider.supports(request.rail()))
                .findFirst()
                .orElseThrow(() -> new InquiryProviderNotConfiguredException(request.providerId()))
                .inquire(request);
        }

        List<PaymentStatusInquiryPort> candidates = providers.stream()
            .filter(provider -> provider.supports(request.rail())).toList();
        if (candidates.isEmpty()) throw new InquiryProviderNotConfiguredException(request.providerId());
        boolean atLeastOneResponse = false;
        for (PaymentStatusInquiryPort provider : candidates) {
            try {
                var providerRequest = new InquiryRequest(request.paymentId(), provider.providerId(),
                    request.externalReference(), request.amount(), request.currency(), request.rail());
                var outcome = provider.inquire(providerRequest);
                atLeastOneResponse = true;
                if (outcome.status() == PaymentStatusInquiryPort.OutcomeStatus.ACCEPTED) return outcome;
            } catch (PaymentStatusInquiryPort.InquiryUnavailableException ignored) {
                // A read-only inquiry may safely continue against another eligible provider.
            }
        }
        if (!atLeastOneResponse) throw new PaymentStatusInquiryPort.InquiryUnavailableException(
            "No eligible provider could answer status inquiry");
        return new PaymentStatusInquiryPort.InquiryOutcome(
            PaymentStatusInquiryPort.OutcomeStatus.UNKNOWN, null, "UNKNOWN_PROVIDER_NOT_RESOLVED");
    }

    public static final class InquiryProviderNotConfiguredException extends RuntimeException {
        public InquiryProviderNotConfiguredException(String providerId) {
            super("No status-inquiry adapter configured for provider " + providerId);
        }
    }
}
