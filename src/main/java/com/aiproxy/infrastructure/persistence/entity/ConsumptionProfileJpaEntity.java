package com.aiproxy.infrastructure.persistence.entity;

import com.aiproxy.domain.model.enumeration.SubscriptionTier;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "consumption_profiles")
public class ConsumptionProfileJpaEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(nullable = false, unique = true, length = 64)
    private String userId;

    @Column(nullable = false)
    private int tokensConsumed;

    @Column(nullable = false)
    private int tokensRemaining;

    @Column(nullable = false)
    private int requestsThisMinute;

    @Column(nullable = false)
    private LocalDateTime minuteResetTimestamp;

    @Column(nullable = false)
    private LocalDate monthlyResetDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionTier subscriptionTier;

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
}
