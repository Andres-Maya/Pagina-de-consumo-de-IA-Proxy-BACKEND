package com.aiproxy.domain.model.entity;

import java.time.LocalDate;

public class DailyUsage {
    private String id;
    private String userId;
    private LocalDate date;
    private int tokensConsumed;
    private int requestCount;

    public DailyUsage() {}

    public DailyUsage(String userId, LocalDate date) {
        this.userId = userId;
        this.date = date;
        this.tokensConsumed = 0;
        this.requestCount = 0;
    }

    public void recordConsumption(int tokens) {
        this.tokensConsumed += tokens;
        this.requestCount++;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public int getTokensConsumed() { return tokensConsumed; }
    public void setTokensConsumed(int tokensConsumed) { this.tokensConsumed = tokensConsumed; }
    public int getRequestCount() { return requestCount; }
    public void setRequestCount(int requestCount) { this.requestCount = requestCount; }
}
