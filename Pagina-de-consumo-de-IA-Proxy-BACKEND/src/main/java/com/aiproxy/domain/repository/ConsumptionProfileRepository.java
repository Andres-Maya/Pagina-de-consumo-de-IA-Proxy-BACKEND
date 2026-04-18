package com.aiproxy.domain.repository;

import com.aiproxy.domain.model.entity.ConsumptionProfile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ConsumptionProfileRepository {
    Optional<ConsumptionProfile> findById(String id);
    Optional<ConsumptionProfile> findByUserId(String userId);
    ConsumptionProfile save(ConsumptionProfile profile);
    List<ConsumptionProfile> findAll();
    void resetAllMinuteCounters();
    void resetAllMonthlyConsumptions(LocalDate resetDate);
}
