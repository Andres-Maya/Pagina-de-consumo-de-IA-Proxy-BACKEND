package com.aiproxy.service.validation;

import com.aiproxy.model.GenerationRequest;
import com.aiproxy.model.ValidationResult;

/**
 * Interface for validation components in the request chain.
 * Validators check requests against specific criteria (rate limits, quotas, etc.)
 * and return approval or rejection decisions.
 * 
 * Implementations include:
 * - RateLimitValidator: Enforces per-minute request limits
 * - QuotaValidator: Enforces monthly token limits
 */
public interface RequestValidator {
    
    /**
     * Validates a generation request for a specific user.
     * 
     * @param userId the ID of the user making the request
     * @param request the generation request to validate
     * @return ValidationResult indicating approval or rejection with reason
     */
    ValidationResult validate(String userId, GenerationRequest request);
}
