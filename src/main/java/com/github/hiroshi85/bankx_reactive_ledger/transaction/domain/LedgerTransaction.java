package com.github.hiroshi85.bankx_reactive_ledger.transaction.domain;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("transactions")
public record LedgerTransaction(
    @Id String id, 
    String accountNumber, 
    TransactionType type,
    BigDecimal amount, 
    String status, 
    Instant createdAt
) { }
