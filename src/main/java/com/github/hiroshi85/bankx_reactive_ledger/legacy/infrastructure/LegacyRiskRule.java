package com.github.hiroshi85.bankx_reactive_ledger.legacy.infrastructure;

import com.github.hiroshi85.bankx_reactive_ledger.risk.domain.RiskDecision;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "legacy_risk_rules")
class LegacyRiskRule {
    @Id private String accountNumber;
    @Enumerated(EnumType.STRING) private RiskDecision decision;
    protected LegacyRiskRule() { }
    RiskDecision getDecision() { return decision; }
}
