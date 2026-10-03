package com.github.hiroshi85.bankx_reactive_ledger.transaction.api;

import com.github.hiroshi85.bankx_reactive_ledger.transaction.application.TransactionEvent;
import com.github.hiroshi85.bankx_reactive_ledger.transaction.application.TransactionEventPublisher;
import com.github.hiroshi85.bankx_reactive_ledger.transaction.application.TransactionService;
import com.github.hiroshi85.bankx_reactive_ledger.transaction.domain.TransactionResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class TransactionController {
    private final TransactionService service;
    private final TransactionEventPublisher events;
    public TransactionController(TransactionService service, TransactionEventPublisher events) {
        this.service = service; this.events = events;
    }

    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TransactionResult> create(@Valid @RequestBody CreateTransactionRequest request) {
        return service.create(request.toCommand());
    }

    @GetMapping(value = "/stream/transactions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<TransactionEvent>> stream() {
        return events.events().map(event -> ServerSentEvent.builder(event).event("transaction").build());
    }
}
