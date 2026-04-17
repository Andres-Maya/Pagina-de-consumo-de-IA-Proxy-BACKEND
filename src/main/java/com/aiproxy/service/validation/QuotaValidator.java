package com.aiproxy.service.validation;

import com.aiproxy.model.GenerationRequest;
import com.aiproxy.model.SubscriptionPlan;
import com.aiproxy.model.ValidationResult;
import com.aiproxy.repository.PlanRepository;
import com.aiproxy.repository.UsageRepository;
import org.springframework.stereotype.Service;

/**
 * Validator that enforces monthly token quotas based on subscription plans.
 * This is the second validator in the request chain, after RateLimitValidator.
 * 
 * Monthly quotas:
 * - FREE: 50,000 tokens per month
 * - PRO: 500,000 tokens per month
 * - ENTERPRISE: unlimited tokens
 */
@Service
public class QuotaValidator implements RequestValidator {
    
    private final PlanRepository planRepository;
    private final UsageRepository usageRepository;
    
    public QuotaValidator(PlanRepository planRepository, UsageRepository usageRepository) {
        this.planRepository = planRepository;
        this.usageRepository = usageRepository;
    }
    
    @Override
    public ValidationResult validate(String userId, GenerationRequest request) {
        // Get user's subscription plan
        SubscriptionPlan plan = planRepository.getPlan(userId);
        
        // Check if plan has unlimited tokens (ENTERPRISE)
        if (plan.hasUnlimitedTokens()) {
            return ValidationResult.approved();
        }
        
        // Get current month's token usage
        int currentUsage = usageRepository.getMonthlyTokenUsage(userId);
        int requestedTokens = request.getMaxTokens();
        int limit = plan.getMonthlyTokens();
        
        // Check if sufficient quota remains
        if (currentUsage + requestedTokens > limit) {
            return ValidationResult.rejected("Monthly quota exhausted");
        }
        
        // Deduct tokens and approve
        usageRepository.addTokenUsage(userId, requestedTokens);
        return ValidationResult.approved();
    }
}
