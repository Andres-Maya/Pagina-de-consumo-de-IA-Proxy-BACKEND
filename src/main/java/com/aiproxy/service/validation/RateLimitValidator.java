package com.aiproxy.service.validation;

import com.aiproxy.model.GenerationRequest;
import com.aiproxy.model.SubscriptionPlan;
import com.aiproxy.model.ValidationResult;
import com.aiproxy.repository.PlanRepository;
import com.aiproxy.repository.UsageRepository;
import org.springframework.stereotype.Service;

/**
 * Validator that enforces per-minute request rate limits based on subscription plans.
 * This is the first validator in the request chain.
 * 
 * Rate limits:
 * - FREE: 10 requests per minute
 * - PRO: 60 requests per minute
 * - ENTERPRISE: unlimited requests
 */
@Service
public class RateLimitValidator implements RequestValidator {
    
    private final PlanRepository planRepository;
    private final UsageRepository usageRepository;
    
    public RateLimitValidator(PlanRepository planRepository, UsageRepository usageRepository) {
        this.planRepository = planRepository;
        this.usageRepository = usageRepository;
    }
    
    @Override
    public ValidationResult validate(String userId, GenerationRequest request) {
        // Get user's subscription plan
        SubscriptionPlan plan = planRepository.getPlan(userId);
        
        // Check if plan has unlimited requests (ENTERPRISE)
        if (plan.hasUnlimitedRequests()) {
            usageRepository.incrementRequestCount(userId);
            return ValidationResult.approved();
        }
        
        // Get current minute's request count
        int currentCount = usageRepository.getRequestCount(userId);
        int limit = plan.getRequestsPerMinute();
        
        // Check if limit exceeded
        if (currentCount >= limit) {
            // Calculate seconds until next minute (rate limit reset)
            // Since rate limits reset every minute, retry after 60 seconds
            return ValidationResult.rateLimited(60);
        }
        
        // Increment request count and approve
        usageRepository.incrementRequestCount(userId);
        return ValidationResult.approved();
    }
}
