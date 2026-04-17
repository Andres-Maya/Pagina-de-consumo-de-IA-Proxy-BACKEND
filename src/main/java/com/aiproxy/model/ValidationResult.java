package com.aiproxy.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of a validation operation in the request chain.
 * Used by validators to communicate approval or rejection decisions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationResult {
    
    private boolean approved;
    
    private String rejectionReason;
    
    private Integer retryAfterSeconds;
    
    /**
     * Creates an approved validation result.
     * 
     * @return approved ValidationResult
     */
    public static ValidationResult approved() {
        return new ValidationResult(true, null, null);
    }
    
    /**
     * Creates a rejected validation result with a reason.
     * 
     * @param reason the reason for rejection
     * @return rejected ValidationResult
     */
    public static ValidationResult rejected(String reason) {
        return new ValidationResult(false, reason, null);
    }
    
    /**
     * Creates a rate-limited validation result with retry information.
     * 
     * @param retryAfter seconds until the rate limit resets
     * @return rate-limited ValidationResult
     */
    public static ValidationResult rateLimited(int retryAfter) {
        return new ValidationResult(false, "Rate limit exceeded", retryAfter);
    }
}
