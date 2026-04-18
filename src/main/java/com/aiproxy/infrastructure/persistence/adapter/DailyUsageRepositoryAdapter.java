package com.aiproxy.infrastructure.persistence.adapter;

import com.aiproxy.domain.model.entity.DailyUsage;
import com.aiproxy.domain.repository.DailyUsageRepository;
import com.aiproxy.infrastructure.persistence.entity.DailyUsageJpaEntity;
import com.aiproxy.infrastructure.persistence.repository.DailyUsageJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DailyUsageRepositoryAdapter implements DailyUsageRepository {

    private final DailyUsageJpaRepository jpaRepository;

    public DailyUsageRepositoryAdapter(DailyUsageJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<DailyUsage> findById(String id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<DailyUsage> findByUserIdAndDate(String userId, LocalDate date) {
        return jpaRepository.findByUserIdAndDate(userId, date).map(this::toDomain);
    }

    @Override
    public List<DailyUsage> findByUserIdAndDateBetween(String userId, LocalDate start, LocalDate end) {
        return jpaRepository.findByUserIdAndDateBetween(userId, start, end).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public DailyUsage save(DailyUsage dailyUsage) {
        DailyUsageJpaEntity entity = toEntity(dailyUsage);
        DailyUsageJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    private DailyUsage toDomain(DailyUsageJpaEntity entity) {
        DailyUsage usage = new DailyUsage();
        usage.setId(entity.getId());
        usage.setUserId(entity.getUserId());
        usage.setDate(entity.getDate());
        usage.setTokensConsumed(entity.getTokensConsumed());
        usage.setRequestCount(entity.getRequestCount());
        return usage;
    }

    private DailyUsageJpaEntity toEntity(DailyUsage usage) {
        DailyUsageJpaEntity entity = new DailyUsageJpaEntity();
        entity.setId(usage.getId());
        entity.setUserId(usage.getUserId());
        entity.setDate(usage.getDate());
        entity.setTokensConsumed(usage.getTokensConsumed());
        entity.setRequestCount(usage.getRequestCount());
        return entity;
    }
}
