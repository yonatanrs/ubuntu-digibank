package com.ubuntu.bank.payment.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment", uniqueConstraints = @UniqueConstraint(name = "uk_payment_idempotency", columnNames = "idempotency_key"))
public class Payment {
    @Id private UUID id;
    @Column(name = "idempotency_key", nullable = false, length = 100) private String idempotencyKey;
    @Column(name = "request_fingerprint", nullable = false, length = 64) private String requestFingerprint;
    @Column(nullable = false) private String debtorAccountId;
    @Column(nullable = false) private String creditorAccountId;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amount;
    @Column(nullable = false, length = 3) private String currency;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Rail rail;
    @Column(nullable = false, length = 140) private String reference;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Status status;
    @Column(nullable = false) private Instant createdAt;
    @Column(nullable = false) private Instant updatedAt;
    private String externalProvider;
    private String externalReference;
    private String failureCode;
    @Column(nullable = false) private int inquiryAttempts;
    private Instant nextInquiryAt;
    @Version private long version;

    protected Payment() {}

    public static Payment receive(String key, String fingerprint, String debtor, String creditor,
                                  BigDecimal amount, String currency, Rail rail, String reference) {
        Payment p = new Payment();
        p.id = UUID.randomUUID(); p.idempotencyKey = key; p.requestFingerprint = fingerprint;
        p.debtorAccountId = debtor; p.creditorAccountId = creditor; p.amount = amount;
        p.currency = currency; p.rail = rail; p.reference = reference;
        p.status = Status.RECEIVED; p.createdAt = Instant.now(); p.updatedAt = p.createdAt;
        return p;
    }

    public void markSubmitted(String provider, String externalReference) {
        require(Status.DISPATCHING); this.status = Status.SUBMITTED;
        this.externalProvider = provider; this.externalReference = externalReference; touch();
    }

    public void markRejected(String provider, String externalReference, String code) {
        require(Status.DISPATCHING); this.status = Status.REJECTED;
        this.externalProvider = provider; this.externalReference = externalReference;
        this.failureCode = code; touch();
    }

    public void markPendingConfirmation(String provider, String code) {
        require(Status.DISPATCHING); this.status = Status.PENDING_CONFIRMATION;
        this.externalProvider = provider; this.failureCode = code;
        this.inquiryAttempts = 0; this.nextInquiryAt = Instant.now(); touch();
    }

    public void markFailed(String code) {
        require(Status.DISPATCHING); this.status = Status.FAILED; this.failureCode = code; touch();
    }

    public void resolveReconciliationAsSubmitted(String externalReference) {
        require(Status.RECONCILING); this.status = Status.SUBMITTED;
        if (externalReference != null && !externalReference.isBlank()) this.externalReference = externalReference;
        this.failureCode = null; this.nextInquiryAt = null; touch();
    }

    public void resolveReconciliationAsRejected(String externalReference, String reasonCode) {
        require(Status.RECONCILING); this.status = Status.REJECTED;
        if (externalReference != null && !externalReference.isBlank()) this.externalReference = externalReference;
        this.failureCode = reasonCode; this.nextInquiryAt = null; touch();
    }

    public void rescheduleReconciliation(String reasonCode, int maxAttempts) {
        require(Status.RECONCILING); inquiryAttempts++;
        this.failureCode = reasonCode;
        if (inquiryAttempts >= maxAttempts) {
            this.status = Status.MANUAL_REVIEW_REQUIRED; this.nextInquiryAt = null;
        } else {
            this.status = Status.PENDING_CONFIRMATION;
            long delaySeconds = Math.min(900, 15L * (1L << Math.min(inquiryAttempts - 1, 6)));
            this.nextInquiryAt = Instant.now().plusSeconds(delaySeconds);
        }
        touch();
    }

    private void require(Status expected) {
        if (status != expected) throw new IllegalStateException("Expected " + expected + " but was " + status);
    }
    private void touch() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getRequestFingerprint() { return requestFingerprint; }
    public String getDebtorAccountId() { return debtorAccountId; }
    public String getCreditorAccountId() { return creditorAccountId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public Rail getRail() { return rail; }
    public String getReference() { return reference; }
    public Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getExternalProvider() { return externalProvider; }
    public String getExternalReference() { return externalReference; }
    public String getFailureCode() { return failureCode; }
    public int getInquiryAttempts() { return inquiryAttempts; }
    public Instant getNextInquiryAt() { return nextInquiryAt; }

    public enum Rail { INTERNAL, PAYSHAP, EFT, RTC, SAMOS }
    public enum Status { RECEIVED, DISPATCHING, SUBMITTED, PENDING_CONFIRMATION, RECONCILING,
        MANUAL_REVIEW_REQUIRED, SETTLED, REJECTED, FAILED }
}
