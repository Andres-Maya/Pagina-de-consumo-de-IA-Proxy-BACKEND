package com.aiproxy.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request model for AI text generation.
 * Contains the prompt and generation parameters.
 */
public class GenerationRequest {
    
    @NotBlank(message = "Prompt cannot be blank")
    private String prompt;
    
    @NotNull(message = "Max tokens must be specified")
    @Min(value = 1, message = "Max tokens must be at least 1")
    @Max(value = 4096, message = "Max tokens cannot exceed 4096")
    private Integer maxTokens;
    
    @NotNull(message = "Temperature must be specified")
    @Min(value = 0, message = "Temperature must be between 0 and 2")
    @Max(value = 2, message = "Temperature must be between 0 and 2")
    private Double temperature;
    
    public GenerationRequest() {
    }
    
    public GenerationRequest(String prompt, Integer maxTokens, Double temperature) {
        this.prompt = prompt;
        this.maxTokens = maxTokens;
        this.temperature = temperature;
    }
    
    public String getPrompt() {
        return prompt;
    }
    
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
    
    public Integer getMaxTokens() {
        return maxTokens;
    }
    
    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }
    
    public Double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
}
