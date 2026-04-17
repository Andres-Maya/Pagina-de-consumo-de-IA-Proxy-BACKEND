package com.aiproxy.model;

/**
 * Result of a validation operation in the request chain.
 * Used by validators to communicate approval or rejection decisions.
 */
public class ValidationResult {
    
    private boolean approved;
    
    private String rejectionReason;
    
    private Integer retryAfterSeconds;
    
    public ValidationResult() {
    }
    
    public ValidationResult(boolean approved, String rejectionReason, Integer retryAfterSeconds) {
        this.approved = approved;
        this.rejectionReason = rejectionReason;
        this.retryAfterSeconds = retryAfterSeconds;
    }
    
    public boolean isApproved() {
        return approved;
    }
    
    public void setApproved(boolean approved) {
        this.approved = approved;
    }
    
    public String getRejectionReason() {
        return rejectionReason;
    }
    
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
    
    public Integer getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
    
    public void setRetryAfterSeconds(Integer retryAfterSeconds) {
        this.retryAfterSeconds = retryAfterSeconds;
    }
    
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
