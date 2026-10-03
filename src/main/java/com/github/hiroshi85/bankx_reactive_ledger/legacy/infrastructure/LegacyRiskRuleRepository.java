package com.github.hiroshi85.bankx_reactive_ledger.legacy.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

interface LegacyRiskRuleRepository extends JpaRepository<LegacyRiskRule, String> { }
