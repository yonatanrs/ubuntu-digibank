package com.ubuntu.bank.payment.application;

import com.ubuntu.bank.payment.domain.Payment;
import java.math.BigDecimal;

public record CreatePaymentCommand(String debtorAccountId, String creditorAccountId,
                                   BigDecimal amount, String currency,
                                   Payment.Rail rail, String reference) {}

