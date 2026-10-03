package com.github.hiroshi85.bankx_reactive_ledger.risk.application;

import com.github.hiroshi85.bankx_reactive_ledger.risk.domain.RiskDecision;
import com.github.hiroshi85.bankx_reactive_ledger.risk.domain.RiskRequest;
import reactor.core.publisher.Mono;

public interface RemoteRiskPort { 
    Mono<RiskDecision> evaluate(RiskRequest request); 
}
