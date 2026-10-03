package com.github.hiroshi85.bankx_reactive_ledger.account.infrastructure;

import com.github.hiroshi85.bankx_reactive_ledger.account.domain.Account;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
class DemoAccountInitializer {
    private static final Logger log = LoggerFactory.getLogger(DemoAccountInitializer.class);
    private final AccountRepository repository;

    DemoAccountInitializer(AccountRepository repository) { 
        this.repository = repository; 
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        Flux.fromIterable(List.of(
                new Account(null, "001-0001", new BigDecimal("1000")),
                new Account(null, "001-0002", new BigDecimal("100")),
                new Account(null, "001-0003", BigDecimal.ZERO)))
            .concatMap(seed -> 
                repository.findByAccountNumber(seed.getAccountNumber())
                    .switchIfEmpty(repository.save(seed))
            )
            .then()
            .subscribe(
                ignored -> { }, 
                error -> log.error("Could not initialize demo accounts", error),
                () -> log.info("Demo accounts are ready")
            );
    }
}
