package com.aiproxy.model;

/**
 * Response model for AI text generation.
 * Contains the generated text and usage metrics.
 */
public class GenerationResponse {
    
    private String text;
    
    private int tokensUsed;
    
    private long processingTimeMs;
    
    public GenerationResponse() {
    }
    
    public GenerationResponse(String text, int tokensUsed, long processingTimeMs) {
        this.text = text;
        this.tokensUsed = tokensUsed;
        this.processingTimeMs = processingTimeMs;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public int getTokensUsed() {
        return tokensUsed;
    }
    
    public void setTokensUsed(int tokensUsed) {
        this.tokensUsed = tokensUsed;
    }
    
    public long getProcessingTimeMs() {
        return processingTimeMs;
    }
    
    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
}
