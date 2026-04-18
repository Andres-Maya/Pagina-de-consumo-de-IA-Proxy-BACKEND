package com.aiproxy.infrastructure.proxy;

import com.aiproxy.domain.exception.ThrottlingExceededException;
import com.aiproxy.domain.model.entity.ConsumptionProfile;
import com.aiproxy.domain.model.enumeration.SubscriptionTier;
import com.aiproxy.domain.repository.ConsumptionProfileRepository;
import com.aiproxy.domain.service.TextGeneration;
import com.aiproxy.presentation.dto.GenerationRequest;
import com.aiproxy.presentation.dto.GenerationResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component("requestThrottlingGuard")
public class RequestThrottlingGuard implements TextGeneration {

    private final TextGeneration nextHandler;
    private final ConsumptionProfileRepository consumptionRepository;

    public RequestThrottlingGuard(@Qualifier("tokenConsumptionGuard") TextGeneration nextHandler,
                                   ConsumptionProfileRepository consumptionRepository) {
        this.nextHandler = nextHandler;
        this.consumptionRepository = consumptionRepository;
    }

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        String userId = request.getUserId();
        ConsumptionProfile profile = consumptionRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Consumption profile not found for user: " + userId));

        SubscriptionTier tier = profile.getSubscriptionTier();

        if (tier == SubscriptionTier.ENTERPRISE) {
            return nextHandler.generate(request);
        }

        int maxRequests = tier.getMaxRequestsPerMinute();
        int currentRequests = profile.getRequestsThisMinute();
        LocalDateTime resetTime = profile.getMinuteResetTimestamp();

        if (currentRequests >= maxRequests) {
            long secondsSinceReset = ChronoUnit.SECONDS.between(resetTime, LocalDateTime.now());
            long retryAfter = Math.max(1, 60 - secondsSinceReset);
            throw new ThrottlingExceededException(
                    "Rate limit exceeded. Maximum " + maxRequests + " requests per minute allowed for " + tier.name() + " plan.",
                    retryAfter
            );
        }

        profile.incrementRequestCount();
        consumptionRepository.save(profile);

        return nextHandler.generate(request);
    }
}
