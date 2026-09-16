package com.ubuntu.bank.payment.api;

import com.ubuntu.bank.payment.application.PaymentService.IdempotencyConflictException;
import com.ubuntu.bank.payment.application.PaymentService.PaymentNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.slf4j.MDC;
import java.util.LinkedHashMap;
import java.util.Map;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException failure) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setTitle("Request validation failed");
        Map<String, String> violations = new LinkedHashMap<>();
        failure.getBindingResult().getFieldErrors().forEach(error ->
            violations.putIfAbsent(error.getField(), error.getDefaultMessage()));
        detail.setProperty("violations", violations);
        detail.setProperty("correlationId", MDC.get("correlationId"));
        return detail;
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ProblemDetail handleOptimisticConflict() {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        detail.setTitle("Concurrent payment update");
        detail.setDetail("The payment changed concurrently. Read the latest state before retrying.");
        detail.setProperty("correlationId", MDC.get("correlationId"));
        return detail;
    }
}
