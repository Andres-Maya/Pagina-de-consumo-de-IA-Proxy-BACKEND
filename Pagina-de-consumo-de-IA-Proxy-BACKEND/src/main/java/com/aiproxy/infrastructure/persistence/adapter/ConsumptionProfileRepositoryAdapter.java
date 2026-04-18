package com.aiproxy.infrastructure.persistence.adapter;

import com.aiproxy.domain.model.entity.ConsumptionProfile;
import com.aiproxy.domain.repository.ConsumptionProfileRepository;
import com.aiproxy.infrastructure.persistence.entity.ConsumptionProfileJpaEntity;
import com.aiproxy.infrastructure.persistence.repository.ConsumptionProfileJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ConsumptionProfileRepositoryAdapter implements ConsumptionProfileRepository {

    private final ConsumptionProfileJpaRepository jpaRepository;

    public ConsumptionProfileRepositoryAdapter(ConsumptionProfileJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<ConsumptionProfile> findById(String id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<ConsumptionProfile> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public ConsumptionProfile save(ConsumptionProfile profile) {
        ConsumptionProfileJpaEntity entity = toEntity(profile);
        ConsumptionProfileJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<ConsumptionProfile> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void resetAllMinuteCounters() {
        jpaRepository.resetAllMinuteCounters();
    }

    @Override
    @Transactional
    public void resetAllMonthlyConsumptions(LocalDate resetDate) {
        jpaRepository.resetAllMonthlyConsumptions(resetDate.plusMonths(1).withDayOfMonth(1));
    }

    private ConsumptionProfile toDomain(ConsumptionProfileJpaEntity entity) {
        ConsumptionProfile profile = new ConsumptionProfile();
        profile.setId(entity.getId());
        profile.setUserId(entity.getUserId());
        profile.setTokensConsumed(entity.getTokensConsumed());
        profile.setTokensRemaining(entity.getTokensRemaining());
        profile.setRequestsThisMinute(entity.getRequestsThisMinute());
        profile.setMinuteResetTimestamp(entity.getMinuteResetTimestamp());
        profile.setMonthlyResetDate(entity.getMonthlyResetDate());
        profile.setSubscriptionTier(entity.getSubscriptionTier());
        return profile;
    }

    private ConsumptionProfileJpaEntity toEntity(ConsumptionProfile profile) {
        ConsumptionProfileJpaEntity entity = new ConsumptionProfileJpaEntity();
        entity.setId(profile.getId());
        entity.setUserId(profile.getUserId());
        entity.setTokensConsumed(profile.getTokensConsumed());
        entity.setTokensRemaining(profile.getTokensRemaining());
        entity.setRequestsThisMinute(profile.getRequestsThisMinute());
        entity.setMinuteResetTimestamp(profile.getMinuteResetTimestamp());
        entity.setMonthlyResetDate(profile.getMonthlyResetDate());
        entity.setSubscriptionTier(profile.getSubscriptionTier());
        return entity;
    }
}
