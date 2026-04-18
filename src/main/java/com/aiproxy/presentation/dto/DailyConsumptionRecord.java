package com.aiproxy.presentation.dto;

import java.time.LocalDate;

public class DailyConsumptionRecord {

    private final LocalDate date;
    private final int tokensConsumed;
    private final int requestCount;

    public DailyConsumptionRecord(LocalDate date, int tokensConsumed, int requestCount) {
        this.date = date;
        this.tokensConsumed = tokensConsumed;
        this.requestCount = requestCount;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getTokensConsumed() {
        return tokensConsumed;
    }

    public int getRequestCount() {
        return requestCount;
    }
}
