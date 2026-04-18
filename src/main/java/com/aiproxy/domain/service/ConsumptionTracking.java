package com.aiproxy.domain.service;

import com.aiproxy.domain.model.entity.DailyUsage;
import com.aiproxy.domain.model.valueobject.TokenCount;
import com.aiproxy.presentation.dto.ConsumptionStatus;

import java.util.List;

public interface ConsumptionTracking {
    ConsumptionStatus checkStatus(String userId);
    void recordUsage(String userId, TokenCount tokens);
    List<DailyUsage> getWeeklyHistory(String userId);
}
