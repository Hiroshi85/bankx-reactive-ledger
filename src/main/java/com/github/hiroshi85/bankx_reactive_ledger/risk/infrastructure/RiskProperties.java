package com.github.hiroshi85.bankx_reactive_ledger.risk.infrastructure;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("risk")
public record RiskProperties(
    String baseUrl, 
    Duration timeout
) { }
