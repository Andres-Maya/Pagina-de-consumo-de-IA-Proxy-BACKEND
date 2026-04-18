package com.aiproxy.domain.model.enumeration;

public enum SubscriptionTier {
    FREE(10, 50000),
    PRO(60, 500000),
    ENTERPRISE(Integer.MAX_VALUE, Integer.MAX_VALUE);

    private final int maxRequestsPerMinute;
    private final int maxTokensPerMonth;

    SubscriptionTier(int maxRequestsPerMinute, int maxTokensPerMonth) {
        this.maxRequestsPerMinute = maxRequestsPerMinute;
        this.maxTokensPerMonth = maxTokensPerMonth;
    }

    public int getMaxRequestsPerMinute() {
        return maxRequestsPerMinute;
    }

    public int getMaxTokensPerMonth() {
        return maxTokensPerMonth;
    }
}
