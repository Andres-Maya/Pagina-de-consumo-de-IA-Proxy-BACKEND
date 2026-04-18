package com.aiproxy.presentation.mapper;

import com.aiproxy.domain.model.entity.DailyUsage;
import com.aiproxy.presentation.dto.DailyConsumptionRecord;
import org.springframework.stereotype.Component;

@Component
public class ConsumptionMapper {

    public DailyConsumptionRecord toDailyRecord(DailyUsage usage) {
        return new DailyConsumptionRecord(
                usage.getDate(),
                usage.getTokensConsumed(),
                usage.getRequestCount()
        );
    }
}
