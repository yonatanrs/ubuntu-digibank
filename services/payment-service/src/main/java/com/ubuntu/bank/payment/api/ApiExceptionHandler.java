package com.ubuntu.bank.payment.api;

import com.ubuntu.bank.payment.application.PaymentService.IdempotencyConflictException;
import com.ubuntu.bank.payment.application.PaymentService.PaymentNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IdempotencyConflictException.class)
    ProblemDetail handleConflict() {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        detail.setTitle("Idempotency conflict");
        detail.setDetail("The idempotency key was already used with a different request.");
        return detail;
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    ProblemDetail handleNotFound() {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        detail.setTitle("Payment not found");
        detail.setDetail("No payment exists for the supplied identifier.");
        return detail;
    }
}
