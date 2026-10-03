package com.github.hiroshi85.bankx_reactive_ledger.transaction.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.hiroshi85.bankx_reactive_ledger.account.domain.Account;
import com.github.hiroshi85.bankx_reactive_ledger.account.infrastructure.AccountRepository;
import com.github.hiroshi85.bankx_reactive_ledger.common.error.BusinessException;
import com.github.hiroshi85.bankx_reactive_ledger.risk.application.RiskEvaluationService;
import com.github.hiroshi85.bankx_reactive_ledger.risk.domain.RiskDecision;
import com.github.hiroshi85.bankx_reactive_ledger.transaction.domain.CreateTransactionCommand;
import com.github.hiroshi85.bankx_reactive_ledger.transaction.domain.LedgerTransaction;
import com.github.hiroshi85.bankx_reactive_ledger.transaction.domain.TransactionType;
import com.github.hiroshi85.bankx_reactive_ledger.transaction.infrastructure.TransactionRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class TransactionServiceTest {
    private AccountRepository accounts;
    private TransactionRepository transactions;
    private RiskEvaluationService risk;
    private TransactionEventPublisher events;
    private TransactionService service;

    @BeforeEach void setUp() {
        accounts = mock(AccountRepository.class);
        transactions = mock(TransactionRepository.class);
        risk = mock(RiskEvaluationService.class);
        events = new TransactionEventPublisher();
        service = new TransactionService(accounts, transactions, risk, events);
        when(accounts.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(transactions.save(any())).thenAnswer(invocation -> {
            LedgerTransaction tx = invocation.getArgument(0);
            return Mono.just(new LedgerTransaction("tx-1", tx.accountNumber(), tx.type(), tx.amount(), tx.status(), tx.createdAt()));
        });
    }

    @Test void createsCreditAndIncreasesBalance() {
        givenAccount("100"); givenApproved();
        StepVerifier.create(service.create(command(TransactionType.CREDIT, "25")))
            .assertNext(result -> { assertThat(result.status()).isEqualTo("OK"); assertThat(result.balance()).isEqualByComparingTo("125"); })
            .verifyComplete();
        verify(transactions).save(any());
    }

    @Test void createsDebitAndDecreasesBalance() {
        givenAccount("100"); givenApproved();
        StepVerifier.create(service.create(command(TransactionType.DEBIT, "25")))
            .assertNext(result -> assertThat(result.balance()).isEqualByComparingTo("75")).verifyComplete();
    }

    @Test void rejectsUnknownAccount() {
        when(accounts.findByAccountNumber("001-0001")).thenReturn(Mono.empty());
        StepVerifier.create(service.create(command(TransactionType.CREDIT, "25")))
            .expectErrorSatisfies(error -> assertCode(error, "account_not_found")).verify();
    }

    @Test void rejectsInsufficientFunds() {
        givenAccount("10"); givenApproved();
        StepVerifier.create(service.create(command(TransactionType.DEBIT, "25")))
            .expectErrorSatisfies(error -> assertCode(error, "insufficient_funds")).verify();
    }

    @Test void rejectsFunctionalRiskDecision() {
        givenAccount("100"); when(risk.evaluate(any())).thenReturn(Mono.just(RiskDecision.REJECTED));
        StepVerifier.create(service.create(command(TransactionType.CREDIT, "25")))
            .expectErrorSatisfies(error -> assertCode(error, "risk_rejected")).verify();
    }

    @Test void successfulTransactionPublishesSseEventAfterPersistence() {
        givenAccount("100"); givenApproved();
        StepVerifier.create(Mono.zip(events.events().next(), service.create(command(TransactionType.CREDIT, "25"))))
            .assertNext(tuple -> {
                assertThat(tuple.getT1().transactionId()).isEqualTo("tx-1");
                assertThat(tuple.getT1().status()).isEqualTo("OK");
                assertThat(tuple.getT2().balance()).isEqualByComparingTo("125");
            }).verifyComplete();
    }

    private void givenAccount(String balance) {
        when(accounts.findByAccountNumber("001-0001"))
            .thenReturn(Mono.just(new Account("account-1", "001-0001", new BigDecimal(balance))));
    }
    private void givenApproved() { when(risk.evaluate(any())).thenReturn(Mono.just(RiskDecision.APPROVED)); }
    private CreateTransactionCommand command(TransactionType type, String amount) {
        return new CreateTransactionCommand("001-0001", type, new BigDecimal(amount), null);
    }
    private void assertCode(Throwable error, String code) {
        assertThat(error).isInstanceOf(BusinessException.class);
        assertThat(((BusinessException) error).code()).isEqualTo(code);
    }
}
