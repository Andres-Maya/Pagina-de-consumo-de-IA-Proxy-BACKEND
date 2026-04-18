package com.aiproxy.presentation.dto;

public class ConsumptionStatus {

    private final String userId;
    private final String currentPlan;
    private final int tokensUsed;
    private final int tokensRemaining;
    private final int monthlyLimit;
    private final String resetDate;

    public ConsumptionStatus(String userId, String currentPlan, int tokensUsed,
                             int tokensRemaining, int monthlyLimit, String resetDate) {
        this.userId = userId;
        this.currentPlan = currentPlan;
        this.tokensUsed = tokensUsed;
        this.tokensRemaining = tokensRemaining;
        this.monthlyLimit = monthlyLimit;
        this.resetDate = resetDate;
    }

    public String getUserId() {
        return userId;
    }

    public String getCurrentPlan() {
        return currentPlan;
    }

    public int getTokensUsed() {
        return tokensUsed;
    }

    public int getTokensRemaining() {
        return tokensRemaining;
    }

    public int getMonthlyLimit() {
        return monthlyLimit;
    }

    public String getResetDate() {
        return resetDate;
    }
}
