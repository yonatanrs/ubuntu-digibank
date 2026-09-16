package com.ubuntu.bank.payment;

import com.ubuntu.bank.payment.application.CreatePaymentCommand;
import com.ubuntu.bank.payment.application.PaymentService;
import com.ubuntu.bank.payment.domain.Payment;
import com.ubuntu.bank.payment.domain.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.math.BigDecimal;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("local")
@SpringBootTest(properties = "bank.scheduling.enabled=false")
class ConcurrentIdempotencyIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired PaymentService service;
    @Autowired PaymentRepository payments;

    @Test
    void concurrentSameKeyAndPayloadProduceOneBusinessPayment() throws Exception {
        CreatePaymentCommand command = new CreatePaymentCommand("ZA-001", "ZA-002",
            new BigDecimal("125.50"), "ZAR", Payment.Rail.PAYSHAP, "Concurrent test");
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            Callable<Payment> call = () -> { start.await(); return service.create("same-key-0001", command); };
            Future<Payment> first = pool.submit(call);
            Future<Payment> second = pool.submit(call);
            start.countDown();
            assertThat(first.get(10, TimeUnit.SECONDS).getId())
                .isEqualTo(second.get(10, TimeUnit.SECONDS).getId());
        }
        assertThat(payments.count()).isEqualTo(1);
    }
}
