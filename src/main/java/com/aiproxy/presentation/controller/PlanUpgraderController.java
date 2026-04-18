package com.aiproxy.presentation.controller;

import com.aiproxy.application.service.PlanTransitioner;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quota")
public class PlanUpgraderController {

    private final PlanTransitioner planTransitioner;

    public PlanUpgraderController(PlanTransitioner planTransitioner) {
        this.planTransitioner = planTransitioner;
    }

    @PostMapping("/upgrade")
    public ResponseEntity<Void> upgradePlan(@RequestParam String userId) {
        planTransitioner.upgradeToPro(userId);
        return ResponseEntity.ok().build();
    }
}
