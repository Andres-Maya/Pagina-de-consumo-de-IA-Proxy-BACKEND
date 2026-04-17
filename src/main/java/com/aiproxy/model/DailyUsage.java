package com.aiproxy.model;

import java.time.LocalDate;

/**
 * Response model for daily usage statistics.
 * Represents token consumption for a specific date.
 */
public class DailyUsage {
    
    private LocalDate date;
    
    private int tokensUsed;
    
    public DailyUsage() {
    }
    
    public DailyUsage(LocalDate date, int tokensUsed) {
        this.date = date;
        this.tokensUsed = tokensUsed;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public int getTokensUsed() {
        return tokensUsed;
    }
    
    public void setTokensUsed(int tokensUsed) {
        this.tokensUsed = tokensUsed;
    }
}
