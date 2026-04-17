package com.aiproxy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response model for AI text generation.
 * Contains the generated text and usage metrics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerationResponse {
    
    private String text;
    
    private int tokensUsed;
    
    private long processingTimeMs;
}
