package com.aiproxy.model;

/**
 * Enum representing subscription plan tiers with their associated limits.
 * Each plan defines rate limits (requests per minute) and monthly token quotas.
 */
public enum SubscriptionPlan {
    
    /**
     * Free tier: 10 requests per minute, 50,000 tokens per month
     */
    FREE(10, 50_000),
    
    /**
     * Pro tier: 60 requests per minute, 500,000 tokens per month
     */
    PRO(60, 500_000),
    
    /**
     * Enterprise tier: unlimited requests and tokens
     */
    ENTERPRISE(Integer.MAX_VALUE, Integer.MAX_VALUE);
    
    private final int requestsPerMinute;
    private final int monthlyTokens;
    
    SubscriptionPlan(int requestsPerMinute, int monthlyTokens) {
        this.requestsPerMinute = requestsPerMinute;
        this.monthlyTokens = monthlyTokens;
    }
    
    /**
     * Gets the maximum number of requests allowed per minute for this plan.
     * 
     * @return requests per minute limit
     */
    public int getRequestsPerMinute() {
        return requestsPerMinute;
    }
    
    /**
     * Gets the maximum number of tokens allowed per month for this plan.
     * 
     * @return monthly token limit
     */
    public int getMonthlyTokens() {
        return monthlyTokens;
    }
    
    /**
     * Checks if this plan has unlimited requests.
     * 
     * @return true if requests are unlimited, false otherwise
     */
    public boolean hasUnlimitedRequests() {
        return requestsPerMinute == Integer.MAX_VALUE;
    }
    
    /**
     * Checks if this plan has unlimited tokens.
     * 
     * @return true if tokens are unlimited, false otherwise
     */
    public boolean hasUnlimitedTokens() {
        return monthlyTokens == Integer.MAX_VALUE;
    }
}
