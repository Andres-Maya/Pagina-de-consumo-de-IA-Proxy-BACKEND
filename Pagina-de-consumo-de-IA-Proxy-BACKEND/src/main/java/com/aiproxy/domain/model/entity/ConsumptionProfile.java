package com.aiproxy.domain.model.entity;

import com.aiproxy.domain.model.enumeration.SubscriptionTier;
import com.aiproxy.domain.model.valueobject.RequestCount;
import com.aiproxy.domain.model.valueobject.TokenCount;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ConsumptionProfile {
    private String id;
    private String userId;
    private int tokensConsumed;
    private int tokensRemaining;
    private int requestsThisMinute;
    private LocalDateTime minuteResetTimestamp;
    private LocalDate monthlyResetDate;
    private SubscriptionTier subscriptionTier;

    public ConsumptionProfile() {
        this.tokensConsumed = 0;
        this.tokensRemaining = 0;
        this.requestsThisMinute = 0;
        this.minuteResetTimestamp = LocalDateTime.now();
        this.monthlyResetDate = LocalDate.now().plusMonths(1).withDayOfMonth(1);
    }

    public void initializeForTier(SubscriptionTier tier) {
        this.subscriptionTier = tier;
        this.tokensRemaining = tier.getMaxTokensPerMonth();
    }

    public void deductTokens(TokenCount tokens) {
        if (subscriptionTier == SubscriptionTier.ENTERPRISE) return;
        int newRemaining = Math.max(0, this.tokensRemaining - tokens.getValue());
        this.tokensConsumed += (this.tokensRemaining - newRemaining);
        this.tokensRemaining = newRemaining;
    }

    public void incrementRequestCount() {
        this.requestsThisMinute++;
    }

    public void resetMinuteCounter() {
        this.requestsThisMinute = 0;
        this.minuteResetTimestamp = LocalDateTime.now();
    }

    public void resetMonthlyConsumption() {
        if (subscriptionTier != null && subscriptionTier != SubscriptionTier.ENTERPRISE) {
            this.tokensConsumed = 0;
            this.tokensRemaining = subscriptionTier.getMaxTokensPerMonth();
            this.monthlyResetDate = LocalDate.now().plusMonths(1).withDayOfMonth(1);
        }
    }

    public boolean isMonthlyResetDue() {
        return LocalDate.now().isAfter(this.monthlyResetDate) || LocalDate.now().isEqual(this.monthlyResetDate);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public int getTokensConsumed() { return tokensConsumed; }
    public void setTokensConsumed(int tokensConsumed) { this.tokensConsumed = tokensConsumed; }
    public int getTokensRemaining() { return tokensRemaining; }
    public void setTokensRemaining(int tokensRemaining) { this.tokensRemaining = tokensRemaining; }
    public int getRequestsThisMinute() { return requestsThisMinute; }
    public void setRequestsThisMinute(int requestsThisMinute) { this.requestsThisMinute = requestsThisMinute; }
    public LocalDateTime getMinuteResetTimestamp() { return minuteResetTimestamp; }
    public void setMinuteResetTimestamp(LocalDateTime minuteResetTimestamp) { this.minuteResetTimestamp = minuteResetTimestamp; }
    public LocalDate getMonthlyResetDate() { return monthlyResetDate; }
    public void setMonthlyResetDate(LocalDate monthlyResetDate) { this.monthlyResetDate = monthlyResetDate; }
    public SubscriptionTier getSubscriptionTier() { return subscriptionTier; }
    public void setSubscriptionTier(SubscriptionTier subscriptionTier) { this.subscriptionTier = subscriptionTier; }

    public TokenCount getTokensRemainingAsValueObject() {
        return new TokenCount(this.tokensRemaining);
    }

    public RequestCount getRequestsThisMinuteAsValueObject() {
        return new RequestCount(this.requestsThisMinute, this.minuteResetTimestamp != null 
            ? this.minuteResetTimestamp.atZone(java.time.ZoneId.systemDefault()).toInstant() 
            : java.time.Instant.now());
    }
}
