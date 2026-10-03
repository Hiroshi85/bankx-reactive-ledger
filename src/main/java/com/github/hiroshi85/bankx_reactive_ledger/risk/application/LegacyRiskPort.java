package com.github.hiroshi85.bankx_reactive_ledger.risk.application;

import com.github.hiroshi85.bankx_reactive_ledger.risk.domain.RiskDecision;
import com.github.hiroshi85.bankx_reactive_ledger.risk.domain.RiskRequest;

public interface LegacyRiskPort { 
    RiskDecision evaluate(RiskRequest request); 
}
