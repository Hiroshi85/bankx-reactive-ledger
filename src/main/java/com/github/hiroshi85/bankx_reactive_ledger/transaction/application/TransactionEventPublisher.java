package com.github.hiroshi85.bankx_reactive_ledger.transaction.application;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Component
public class TransactionEventPublisher {
    private final Sinks.Many<TransactionEvent> sink = Sinks.many().multicast().directBestEffort();

    public void publish(TransactionEvent event) { 
        sink.tryEmitNext(event); 
    }

    public Flux<TransactionEvent> events() { 
        return sink.asFlux(); 
    }
}
