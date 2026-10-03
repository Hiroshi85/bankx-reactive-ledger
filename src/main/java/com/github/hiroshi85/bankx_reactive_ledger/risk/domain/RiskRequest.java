package com.github.hiroshi85.bankx_reactive_ledger.risk.domain;

import com.github.hiroshi85.bankx_reactive_ledger.transaction.domain.TransactionType;
import java.math.BigDecimal;

public record RiskRequest(String accountNumber, TransactionType type, BigDecimal amount, String simulate) { }
