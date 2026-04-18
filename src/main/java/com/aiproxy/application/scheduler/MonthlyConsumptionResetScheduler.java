package com.aiproxy.application.scheduler;

import com.aiproxy.domain.repository.ConsumptionProfileRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class MonthlyConsumptionResetScheduler {

    private final ConsumptionProfileRepository consumptionRepository;

    public MonthlyConsumptionResetScheduler(ConsumptionProfileRepository consumptionRepository) {
        this.consumptionRepository = consumptionRepository;
    }

    @Scheduled(cron = "0 0 1 1 * *")
    @Transactional
    public void resetMonthlyQuotas() {
        consumptionRepository.resetAllMonthlyConsumptions(LocalDate.now());
    }
}
