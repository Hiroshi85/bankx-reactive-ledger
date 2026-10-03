package com.github.hiroshi85.bankx_reactive_ledger.transaction.api;

import static org.mockito.Mockito.mock;

import com.github.hiroshi85.bankx_reactive_ledger.common.error.GlobalErrorHandler;
import com.github.hiroshi85.bankx_reactive_ledger.transaction.application.TransactionEventPublisher;
import com.github.hiroshi85.bankx_reactive_ledger.transaction.application.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

class TransactionControllerTest {
    private WebTestClient client;

    @BeforeEach void setUp() {
        var controller = new TransactionController(mock(TransactionService.class), new TransactionEventPublisher());
        client = WebTestClient.bindToController(controller).controllerAdvice(new GlobalErrorHandler()).build();
    }

    @Test void invalidAmountReturnsHomogeneousValidationError() {
        client.post().uri("/api/transactions").header("Content-Type", "application/json")
            .bodyValue("{\"accountNumber\":\"001-0001\",\"type\":\"DEBIT\",\"amount\":0}")
            .exchange().expectStatus().isBadRequest().expectBody()
            .jsonPath("$.error").isEqualTo("validation_error");
    }
}
