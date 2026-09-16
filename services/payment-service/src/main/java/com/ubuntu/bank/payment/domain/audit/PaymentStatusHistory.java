package com.ubuntu.bank.payment.domain.audit;

import com.ubuntu.bank.payment.domain.Payment;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_status_history", indexes = @Index(name = "ix_payment_history", columnList = "payment_id,created_at"))
public class PaymentStatusHistory {
    @Id private UUID id;
    @Column(nullable = false) private UUID paymentId;
    @Enumerated(EnumType.STRING) private Payment.Status fromStatus;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Payment.Status toStatus;
    @Column(nullable = false, length = 100) private String reason;
    private String providerId;
    @Column(nullable = false) private Instant createdAt;

    protected PaymentStatusHistory() {}
    public PaymentStatusHistory(UUID paymentId, Payment.Status fromStatus, Payment.Status toStatus,
                                String reason, String providerId) {
        this.id = UUID.randomUUID(); this.paymentId = paymentId; this.fromStatus = fromStatus;
        this.toStatus = toStatus; this.reason = reason; this.providerId = providerId;
        this.createdAt = Instant.now();
    }
}
