package com.aiproxy.application.service;

import com.aiproxy.domain.model.entity.ConsumptionProfile;
import com.aiproxy.domain.model.entity.DailyUsage;
import com.aiproxy.domain.model.enumeration.SubscriptionTier;
import com.aiproxy.domain.repository.ConsumptionProfileRepository;
import com.aiproxy.domain.repository.DailyUsageRepository;
import com.aiproxy.domain.service.ConsumptionTracking;
import com.aiproxy.presentation.dto.ConsumptionStatus;
import com.aiproxy.presentation.dto.DailyConsumptionRecord;
import com.aiproxy.presentation.mapper.ConsumptionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ConsumptionAnalyzer implements ConsumptionTracking {

    private final ConsumptionProfileRepository consumptionRepository;
    private final DailyUsageRepository dailyUsageRepository;
    private final ConsumptionMapper consumptionMapper;

    public ConsumptionAnalyzer(ConsumptionProfileRepository consumptionRepository,
                               DailyUsageRepository dailyUsageRepository,
                               ConsumptionMapper consumptionMapper) {
        this.consumptionRepository = consumptionRepository;
        this.dailyUsageRepository = dailyUsageRepository;
        this.consumptionMapper = consumptionMapper;
    }

    @Override
    public ConsumptionStatus checkStatus(String userId) {
        ConsumptionProfile profile = consumptionRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Consumption profile not found for user: " + userId));

        SubscriptionTier tier = profile.getSubscriptionTier();
        int limit = tier.getMaxTokensPerMonth();
        int used = profile.getTokensConsumed();
        int remaining = tier == SubscriptionTier.ENTERPRISE ? Integer.MAX_VALUE : profile.getTokensRemaining();

        return new ConsumptionStatus(
                userId,
                tier.name(),
                used,
                remaining,
                limit,
                profile.getMonthlyResetDate().toString()
        );
    }

    @Override
    @Transactional
    public void recordUsage(String userId, com.aiproxy.domain.model.valueobject.TokenCount tokens) {
        // handled by TokenConsumptionGuard for atomicity
    }

    @Override
    public List<DailyUsage> getWeeklyHistory(String userId) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);
        return dailyUsageRepository.findByUserIdAndDateBetween(userId, start, end);
    }

    public List<DailyConsumptionRecord> getLastSevenDays(String userId) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);
        List<DailyUsage> usages = dailyUsageRepository.findByUserIdAndDateBetween(userId, start, end);

        return usages.stream()
                .map(consumptionMapper::toDailyRecord)
                .collect(Collectors.toList());
    }
}
