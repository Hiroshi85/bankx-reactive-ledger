package com.github.hiroshi85.bankx_reactive_ledger.account.infrastructure;

import com.github.hiroshi85.bankx_reactive_ledger.account.domain.Account;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface AccountRepository extends ReactiveMongoRepository<Account, String> {
    Mono<Account> findByAccountNumber(String accountNumber);
}
