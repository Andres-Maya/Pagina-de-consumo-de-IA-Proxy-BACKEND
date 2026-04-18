package com.aiproxy.presentation.dto;

public class GenerationResponse {

    private final String generatedText;
    private final int tokensUsed;
    private final long timestamp;

    public GenerationResponse(String generatedText, int tokensUsed, long timestamp) {
        this.generatedText = generatedText;
        this.tokensUsed = tokensUsed;
        this.timestamp = timestamp;
    }

    public String getGeneratedText() {
        return generatedText;
    }

    public int getTokensUsed() {
        return tokensUsed;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
