package com.aiproxy.infrastructure.persistence.repository;

import com.aiproxy.infrastructure.persistence.entity.ConsumptionProfileJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ConsumptionProfileJpaRepository extends JpaRepository<ConsumptionProfileJpaEntity, String> {
    Optional<ConsumptionProfileJpaEntity> findByUserId(String userId);

    @Modifying
    @Query("UPDATE ConsumptionProfileJpaEntity c SET c.requestsThisMinute = 0, c.minuteResetTimestamp = CURRENT_TIMESTAMP")
    void resetAllMinuteCounters();

    @Modifying
    @Query("UPDATE ConsumptionProfileJpaEntity c SET c.tokensConsumed = 0, c.tokensRemaining = CASE c.subscriptionTier WHEN 'FREE' THEN 50000 WHEN 'PRO' THEN 500000 ELSE c.tokensRemaining END, c.monthlyResetDate = :nextMonth")
    void resetAllMonthlyConsumptions(LocalDate nextMonth);
}
