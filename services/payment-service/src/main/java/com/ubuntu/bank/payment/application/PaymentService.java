package com.ubuntu.bank.payment.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubuntu.bank.payment.domain.Payment;
import com.ubuntu.bank.payment.domain.PaymentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentRepository payments;
    private final PaymentCreateTransaction creator;
    private final ObjectMapper json;

    public PaymentService(PaymentRepository payments, PaymentCreateTransaction creator, ObjectMapper json) {
        this.payments = payments; this.creator = creator; this.json = json;
    }

    public Payment create(String idempotencyKey, CreatePaymentCommand request) {
        String fingerprint = fingerprint(request);
        var existing = payments.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) return verifyFingerprint(existing.get(), fingerprint);
        try {
            return creator.create(idempotencyKey, fingerprint, request);
        } catch (DataIntegrityViolationException concurrentInsert) {
            Payment winner = payments.findByIdempotencyKey(idempotencyKey).orElseThrow(() -> concurrentInsert);
            return verifyFingerprint(winner, fingerprint);
        }
    }

    private Payment verifyFingerprint(Payment payment, String fingerprint) {
        if (!payment.getRequestFingerprint().equals(fingerprint)) throw new IdempotencyConflictException();
        return payment;
    }

    @Transactional(readOnly = true)
    public Payment find(UUID id) {
        return payments.findById(id).orElseThrow(PaymentNotFoundException::new);
    }

    private String fingerprint(CreatePaymentCommand r) {
        try {
            byte[] bytes = json.writeValueAsBytes(r);
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Cannot fingerprint request", e);
        }
    }

    public static class IdempotencyConflictException extends RuntimeException {}
    public static class PaymentNotFoundException extends RuntimeException {}
}
