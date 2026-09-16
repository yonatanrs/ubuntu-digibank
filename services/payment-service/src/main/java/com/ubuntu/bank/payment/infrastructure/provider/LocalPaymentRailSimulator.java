package com.ubuntu.bank.payment.infrastructure.provider;

import com.ubuntu.bank.payment.application.port.out.PaymentRailPort;
import com.ubuntu.bank.payment.domain.Payment;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

/** Local-only deterministic simulator. It must never be enabled in a production profile. */
@Component
@Profile("local")
public class LocalPaymentRailSimulator implements PaymentRailPort {
    @Override public String providerId() { return "local-rail-simulator"; }
    @Override public int priority() { return 1000; }
    @Override public boolean supports(Payment.Rail rail) { return rail != Payment.Rail.SAMOS; }

    @Override
    public SubmissionOutcome submit(PaymentInstruction instruction) {
        String reference = "SIM-" + UUID.nameUUIDFromBytes(
            instruction.paymentId().toString().getBytes(StandardCharsets.UTF_8));
        return new SubmissionOutcome(providerId(), reference, OutcomeStatus.ACCEPTED);
    }
}
