package com.aiproxy.infrastructure.proxy;

import com.aiproxy.domain.exception.InsufficientTokensException;
import com.aiproxy.domain.model.entity.ConsumptionProfile;
import com.aiproxy.domain.model.entity.DailyUsage;
import com.aiproxy.domain.model.enumeration.SubscriptionTier;
import com.aiproxy.domain.model.valueobject.TokenCount;
import com.aiproxy.domain.repository.ConsumptionProfileRepository;
import com.aiproxy.domain.repository.DailyUsageRepository;
import com.aiproxy.domain.service.TextGeneration;
import com.aiproxy.presentation.dto.GenerationRequest;
import com.aiproxy.presentation.dto.GenerationResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component("tokenConsumptionGuard")
public class TokenConsumptionGuard implements TextGeneration {

    private final TextGeneration nextHandler;
    private final ConsumptionProfileRepository consumptionRepository;
    private final DailyUsageRepository dailyUsageRepository;

    public TokenConsumptionGuard(@Qualifier("mockTextGenerator") TextGeneration nextHandler,
                                  ConsumptionProfileRepository consumptionRepository,
                                  DailyUsageRepository dailyUsageRepository) {
        this.nextHandler = nextHandler;
        this.consumptionRepository = consumptionRepository;
        this.dailyUsageRepository = dailyUsageRepository;
    }

    @Override
    @Transactional
    public GenerationResponse generate(GenerationRequest request) {
        String userId = request.getUserId();
        ConsumptionProfile profile = consumptionRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Consumption profile not found for user: " + userId));

        SubscriptionTier tier = profile.getSubscriptionTier();

        if (tier == SubscriptionTier.ENTERPRISE) {
            GenerationResponse response = nextHandler.generate(request);
            recordDailyUsage(userId, response.getTokensUsed());
            return response;
        }

        int maxTokens = tier.getMaxTokensPerMonth();
        int remaining = profile.getTokensRemaining();

        int estimatedTokens = request.getPrompt().length() / 4 + 500;

        if (remaining < estimatedTokens) {
            int used = maxTokens - remaining;
            throw new InsufficientTokensException(
                    "Monthly token quota exhausted. Plan: " + tier.name() +
                    ". Used: " + used + " of " + maxTokens + " tokens."
            );
        }

        GenerationResponse response = nextHandler.generate(request);
        TokenCount actualTokens = new TokenCount(response.getTokensUsed());

        profile.deductTokens(actualTokens);
        consumptionRepository.save(profile);

        recordDailyUsage(userId, response.getTokensUsed());

        return response;
    }

    private void recordDailyUsage(String userId, int tokensUsed) {
        LocalDate today = LocalDate.now();
        DailyUsage todayUsage = dailyUsageRepository.findByUserIdAndDate(userId, today)
                .orElseGet(() -> new DailyUsage(userId, today));

        todayUsage.recordConsumption(tokensUsed);
        dailyUsageRepository.save(todayUsage);
    }
}
