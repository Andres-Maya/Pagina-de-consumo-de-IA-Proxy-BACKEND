package com.aiproxy.domain.repository;

import com.aiproxy.domain.model.entity.DailyUsage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyUsageRepository {
    Optional<DailyUsage> findById(String id);
    Optional<DailyUsage> findByUserIdAndDate(String userId, LocalDate date);
    List<DailyUsage> findByUserIdAndDateBetween(String userId, LocalDate start, LocalDate end);
    DailyUsage save(DailyUsage dailyUsage);
}
