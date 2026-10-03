package com.github.hiroshi85.bankx_reactive_ledger.legacy.infrastructure;

import com.github.hiroshi85.bankx_reactive_ledger.risk.application.LegacyRiskPort;
import com.github.hiroshi85.bankx_reactive_ledger.risk.domain.RiskDecision;
import com.github.hiroshi85.bankx_reactive_ledger.risk.domain.RiskRequest;
import org.springframework.stereotype.Component;

@Component
class JpaLegacyRiskAdapter implements LegacyRiskPort {
    private final LegacyRiskRuleRepository repository;
    JpaLegacyRiskAdapter(LegacyRiskRuleRepository repository) { 
        this.repository = repository; 
    }

    @Override 
    public RiskDecision evaluate(RiskRequest request) {
        return repository.findById(request.accountNumber()).map(LegacyRiskRule::getDecision)
            .orElse(RiskDecision.REJECTED);
    }
}
