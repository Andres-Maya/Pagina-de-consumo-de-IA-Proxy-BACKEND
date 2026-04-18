package com.aiproxy.infrastructure.proxy.chain;

import com.aiproxy.domain.service.TextGeneration;
import com.aiproxy.infrastructure.external.MockTextGenerator;
import com.aiproxy.infrastructure.proxy.RequestThrottlingGuard;
import com.aiproxy.infrastructure.proxy.TokenConsumptionGuard;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class GenerationChainBuilder {

    @Bean
    public TextGeneration mockTextGenerator() {
        return new MockTextGenerator();
    }

    @Bean
    public TextGeneration tokenConsumptionGuard(TextGeneration mockTextGenerator,
                                                 com.aiproxy.domain.repository.ConsumptionProfileRepository consumptionRepository,
                                                 com.aiproxy.domain.repository.DailyUsageRepository dailyUsageRepository) {
        return new TokenConsumptionGuard(mockTextGenerator, consumptionRepository, dailyUsageRepository);
    }

    @Bean
    public TextGeneration requestThrottlingGuard(TextGeneration tokenConsumptionGuard,
                                                  com.aiproxy.domain.repository.ConsumptionProfileRepository consumptionRepository) {
        return new RequestThrottlingGuard(tokenConsumptionGuard, consumptionRepository);
    }

    @Bean
    @Primary
    public TextGeneration textGenerationRouting(TextGeneration requestThrottlingGuard) {
        return new com.aiproxy.infrastructure.proxy.TextGenerationRouting(requestThrottlingGuard);
    }
}
