package com.ubuntu.bank.payment.api;

import com.ubuntu.bank.payment.application.PaymentService;
import com.ubuntu.bank.payment.application.CreatePaymentCommand;
import com.ubuntu.bank.payment.domain.Payment;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentService service;
    public PaymentController(PaymentService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public PaymentResponse create(@RequestHeader("Idempotency-Key") @Size(min = 8, max = 100) String key,
                                  @Valid @RequestBody CreatePaymentRequest request) {
        return PaymentResponse.from(service.create(key, request.toCommand()));
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse get(@PathVariable UUID paymentId) {
        return PaymentResponse.from(service.find(paymentId));
    }

    public record CreatePaymentRequest(
        @NotBlank @Size(max = 64) @Pattern(regexp = "[A-Za-z0-9._:-]+") String debtorAccountId,
        @NotBlank @Size(max = 64) @Pattern(regexp = "[A-Za-z0-9._:-]+") String creditorAccountId,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount,
        @NotNull @Pattern(regexp = "ZAR") String currency,
        @NotNull Payment.Rail rail,
        @NotBlank @Size(max = 140) @Pattern(regexp = "^[^\\p{Cc}\\p{Cf}]+$") String reference) {
        CreatePaymentCommand toCommand() {
            return new CreatePaymentCommand(debtorAccountId, creditorAccountId, amount, currency, rail, reference);
        }
    }

    public record PaymentResponse(UUID paymentId, String status, BigDecimal amount, String currency,
                                  Payment.Rail rail, Instant createdAt, Instant updatedAt,
                                  String externalProvider, String externalReference, String failureCode) {
        static PaymentResponse from(Payment p) {
            return new PaymentResponse(p.getId(), p.getStatus().name(), p.getAmount(),
                p.getCurrency(), p.getRail(), p.getCreatedAt(), p.getUpdatedAt(),
                p.getExternalProvider(), p.getExternalReference(), p.getFailureCode());
        }
    }
}
