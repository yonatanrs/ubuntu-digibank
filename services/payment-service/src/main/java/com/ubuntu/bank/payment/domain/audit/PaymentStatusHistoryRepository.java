package com.ubuntu.bank.payment.domain.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PaymentStatusHistoryRepository extends JpaRepository<PaymentStatusHistory, UUID> {}

