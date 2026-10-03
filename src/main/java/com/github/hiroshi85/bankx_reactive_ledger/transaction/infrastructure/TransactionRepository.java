package com.github.hiroshi85.bankx_reactive_ledger.transaction.infrastructure;

import com.github.hiroshi85.bankx_reactive_ledger.transaction.domain.LedgerTransaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface TransactionRepository extends ReactiveMongoRepository<LedgerTransaction, String> { }
