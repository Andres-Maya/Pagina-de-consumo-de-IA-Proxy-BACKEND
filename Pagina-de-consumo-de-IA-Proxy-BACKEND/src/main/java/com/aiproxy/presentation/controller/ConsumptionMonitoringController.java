package com.aiproxy.presentation.controller;

import com.aiproxy.application.service.ConsumptionAnalyzer;
import com.aiproxy.presentation.dto.ConsumptionStatus;
import com.aiproxy.presentation.dto.DailyConsumptionRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/quota")
public class ConsumptionMonitoringController {

    private final ConsumptionAnalyzer consumptionAnalyzer;

    public ConsumptionMonitoringController(ConsumptionAnalyzer consumptionAnalyzer) {
        this.consumptionAnalyzer = consumptionAnalyzer;
    }

    @GetMapping("/status")
    public ResponseEntity<ConsumptionStatus> getStatus(@RequestParam String userId) {
        ConsumptionStatus status = consumptionAnalyzer.checkStatus(userId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/history")
    public ResponseEntity<List<DailyConsumptionRecord>> getHistory(@RequestParam String userId) {
        List<DailyConsumptionRecord> history = consumptionAnalyzer.getLastSevenDays(userId);
        return ResponseEntity.ok(history);
    }
}
