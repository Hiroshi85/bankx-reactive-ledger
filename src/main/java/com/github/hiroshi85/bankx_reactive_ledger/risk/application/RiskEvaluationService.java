package com.github.hiroshi85.bankx_reactive_ledger.risk.application;

import com.github.hiroshi85.bankx_reactive_ledger.risk.domain.RiskDecision;
import com.github.hiroshi85.bankx_reactive_ledger.risk.domain.RiskRequest;
import com.github.hiroshi85.bankx_reactive_ledger.risk.infrastructure.RiskProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class RiskEvaluationService {
    private static final Logger log = LoggerFactory.getLogger(RiskEvaluationService.class);
    private final RemoteRiskPort remote;
    private final LegacyRiskPort legacy;
    private final RiskProperties properties;

    public RiskEvaluationService(RemoteRiskPort remote, LegacyRiskPort legacy, RiskProperties properties) {
        this.remote = remote; this.legacy = legacy; this.properties = properties;
    }

    public Mono<RiskDecision> evaluate(RiskRequest request) {
        return remote.evaluate(request)
            .timeout(properties.timeout())
            .doOnError(error -> log.warn("Remote risk failed for account={}: {}", request.accountNumber(), error.toString()))
            .onErrorResume(error -> legacyFallback(request));
    }

    private Mono<RiskDecision> legacyFallback(RiskRequest request) {
        log.info("Activating legacy risk fallback for account={}", request.accountNumber());
        // JPA/JDBC is blocking: this is the explicit boundary away from the Netty event loop.
        return Mono.fromCallable(() -> legacy.evaluate(request)).subscribeOn(Schedulers.boundedElastic());
    }
}
