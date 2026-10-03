package com.github.hiroshi85.bankx_reactive_ledger.risk.infrastructure;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(RiskProperties.class)
class RiskClientConfig {
    @Bean WebClient riskWebClient(WebClient.Builder builder, RiskProperties properties) {
        return builder.baseUrl(properties.baseUrl()).build();
    }
}
