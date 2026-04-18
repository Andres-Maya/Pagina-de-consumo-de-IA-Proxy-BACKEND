package com.aiproxy.application.scheduler;

import com.aiproxy.domain.repository.ConsumptionProfileRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ThrottlingResetScheduler {

    private final ConsumptionProfileRepository consumptionRepository;

    public ThrottlingResetScheduler(ConsumptionProfileRepository consumptionRepository) {
        this.consumptionRepository = consumptionRepository;
    }

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void resetMinuteCounters() {
        consumptionRepository.resetAllMinuteCounters();
    }
}
