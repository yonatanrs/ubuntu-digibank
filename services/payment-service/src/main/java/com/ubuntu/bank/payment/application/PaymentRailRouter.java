package com.ubuntu.bank.payment.application;

import com.ubuntu.bank.payment.application.port.out.PaymentRailPort;
import com.ubuntu.bank.payment.application.port.out.PaymentRailPort.PaymentInstruction;
import com.ubuntu.bank.payment.application.port.out.PaymentRailPort.ProviderUnavailableException;
import com.ubuntu.bank.payment.domain.Payment;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;

@Service
public class PaymentRailRouter {
    private final List<PaymentRailPort> providers;

    public PaymentRailRouter(List<PaymentRailPort> providers) {
        this.providers = providers.stream()
            .sorted(Comparator.comparingInt(PaymentRailPort::priority))
            .toList();
    }

    /**
     * Failover is allowed only for a definitive pre-acceptance outage. An indeterminate
     * timeout is deliberately propagated to prevent duplicate external payments.
     */
    public PaymentRailPort.SubmissionOutcome submit(PaymentInstruction instruction) {
        List<PaymentRailPort> eligible = providers.stream()
            .filter(p -> p.supports(instruction.rail()))
            .toList();
        if (eligible.isEmpty()) {
            throw new NoRailProviderException(instruction.rail());
        }

        ProviderUnavailableException lastFailure = null;
        for (PaymentRailPort provider : eligible) {
            try {
                return provider.submit(instruction);
            } catch (ProviderUnavailableException unavailable) {
                lastFailure = unavailable;
            }
        }
        throw new AllRailProvidersUnavailableException(instruction.rail(), lastFailure);
    }

    public static final class NoRailProviderException extends RuntimeException {
        public NoRailProviderException(Payment.Rail rail) { super("No provider configured for rail " + rail); }
    }

    public static final class AllRailProvidersUnavailableException extends RuntimeException {
        public AllRailProvidersUnavailableException(Payment.Rail rail, Throwable cause) {
            super("All providers unavailable for rail " + rail, cause);
        }
    }
}

