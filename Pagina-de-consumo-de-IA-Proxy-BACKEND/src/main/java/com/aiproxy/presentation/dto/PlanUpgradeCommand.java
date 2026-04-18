package com.aiproxy.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public class PlanUpgradeCommand {

    @NotBlank(message = "User ID is required")
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
