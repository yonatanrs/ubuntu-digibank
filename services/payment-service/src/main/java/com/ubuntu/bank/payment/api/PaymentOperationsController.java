package com.ubuntu.bank.payment.api;

import com.ubuntu.bank.payment.application.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/operations/payments")
public class PaymentOperationsController {
    private final PaymentService payments;
    public PaymentOperationsController(PaymentService payments) { this.payments = payments; }

    @GetMapping("/manual-review")
    public Page<PaymentController.PaymentResponse> manualReview(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size) {
        int safeSize = Math.max(1, Math.min(size, 100));
        int safePage = Math.max(0, page);
        return payments.manualReviewQueue(PageRequest.of(safePage, safeSize))
            .map(PaymentController.PaymentResponse::from);
    }
}

