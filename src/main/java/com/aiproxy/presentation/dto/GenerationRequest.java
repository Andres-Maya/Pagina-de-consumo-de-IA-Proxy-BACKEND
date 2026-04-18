package com.aiproxy.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GenerationRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Prompt is required")
    @Size(min = 1, max = 4000, message = "Prompt must be between 1 and 4000 characters")
    private String prompt;

    public GenerationRequest() {}

    public GenerationRequest(String userId, String prompt) {
        this.userId = userId;
        this.prompt = prompt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
