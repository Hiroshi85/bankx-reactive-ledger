package com.github.hiroshi85.bankx_reactive_ledger.risk.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.hiroshi85.bankx_reactive_ledger.risk.domain.RiskDecision;
import com.github.hiroshi85.bankx_reactive_ledger.risk.domain.RiskRequest;
import com.github.hiroshi85.bankx_reactive_ledger.risk.infrastructure.RiskProperties;
import com.github.hiroshi85.bankx_reactive_ledger.transaction.domain.TransactionType;
import java.math.BigDecimal;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class RiskEvaluationServiceTest {
    private RemoteRiskPort remote;
    private LegacyRiskPort legacy;
    private RiskEvaluationService service;
    private final RiskRequest request = new RiskRequest("001-0001", TransactionType.DEBIT, BigDecimal.TEN, null);

    @BeforeEach void setUp() {
        remote = mock(RemoteRiskPort.class); legacy = mock(LegacyRiskPort.class);
        service = new RiskEvaluationService(remote, legacy, new RiskProperties("http://localhost", Duration.ofMillis(30)));
    }

    @Test void functionalRejectionDoesNotUseFallback() {
        when(remote.evaluate(request)).thenReturn(Mono.just(RiskDecision.REJECTED));
        StepVerifier.create(service.evaluate(request)).expectNext(RiskDecision.REJECTED).verifyComplete();
        verify(legacy, never()).evaluate(any());
    }

    @Test void technicalFailureUsesLegacyFallback() {
        when(remote.evaluate(request)).thenReturn(Mono.error(new RuntimeException("remote down")));
        when(legacy.evaluate(request)).thenReturn(RiskDecision.APPROVED);
        StepVerifier.create(service.evaluate(request)).expectNext(RiskDecision.APPROVED).verifyComplete();
        verify(legacy).evaluate(request);
    }

    @Test void timeoutUsesLegacyFallbackOffCallingThread() {
        when(remote.evaluate(request)).thenReturn(Mono.never());
        when(legacy.evaluate(request)).thenAnswer(invocation -> {
            assertThat(Thread.currentThread().getName()).contains("boundedElastic");
            return RiskDecision.APPROVED;
        });
        StepVerifier.create(service.evaluate(request)).expectNext(RiskDecision.APPROVED).verifyComplete();
        verify(legacy).evaluate(request);
    }
}
