package com.github.hiroshi85.bankx_reactive_ledger.transaction.application;

import com.github.hiroshi85.bankx_reactive_ledger.transaction.domain.TransactionType;
import java.math.BigDecimal;

public record TransactionEvent(
    String transactionId, 
    String accountNumber, 
    TransactionType type,
    BigDecimal amount, 
    String status
) { }
