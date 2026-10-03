package com.github.hiroshi85.bankx_reactive_ledger.transaction.api;

import com.github.hiroshi85.bankx_reactive_ledger.transaction.domain.CreateTransactionCommand;
import com.github.hiroshi85.bankx_reactive_ledger.transaction.domain.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateTransactionRequest(
    @NotBlank String accountNumber,
    @NotNull TransactionType type,
    @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal amount,
    String simulate
) {
    CreateTransactionCommand toCommand() {
        return new CreateTransactionCommand(accountNumber, type, amount, simulate); 
    }
}
