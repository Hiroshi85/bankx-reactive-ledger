package com.github.hiroshi85.bankx_reactive_ledger.transaction.domain;

import java.math.BigDecimal;

public record TransactionResult(String id, String accountNumber, TransactionType type,
                                BigDecimal amount, String status, BigDecimal balance) { }
