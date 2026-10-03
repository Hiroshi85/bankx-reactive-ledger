package com.github.hiroshi85.bankx_reactive_ledger.risk.infrastructure;

import com.github.hiroshi85.bankx_reactive_ledger.risk.application.RemoteRiskPort;
import com.github.hiroshi85.bankx_reactive_ledger.risk.domain.RiskDecision;
import com.github.hiroshi85.bankx_reactive_ledger.risk.domain.RiskRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
class WebClientRiskAdapter implements RemoteRiskPort {
    private static final Logger log = LoggerFactory.getLogger(WebClientRiskAdapter.class);
    private final WebClient client;

    WebClientRiskAdapter(WebClient riskWebClient) { 
        this.client = riskWebClient; 
    }

    @Override 
    public Mono<RiskDecision> evaluate(RiskRequest request) {
        return client.post().uri("/risk/evaluate").bodyValue(request).retrieve()
            .bodyToMono(RiskResponse.class)
            .map(RiskResponse::decision)
            .doOnNext(
                decision -> log.info("Remote risk decision={} account={}", decision, request.accountNumber())
            );
    }
    
    private record RiskResponse(RiskDecision decision) { }
}
