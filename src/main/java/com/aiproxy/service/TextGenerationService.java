package com.aiproxy.service;

import com.aiproxy.model.GenerationRequest;
import com.aiproxy.model.GenerationResponse;

/**
 * Core service interface for text generation operations.
 * Defines the contract for AI text generation implementations.
 * 
 * This interface supports polymorphic implementations, allowing different
 * generation strategies (simulated, real AI providers, etc.) to be used
 * interchangeably without changing client code.
 */
public interface TextGenerationService {
    
    /**
     * Generates AI text based on the provided request parameters.
     * 
     * @param request the generation request containing prompt and parameters
     * @return the generation response with generated text and usage metrics
     */
    GenerationResponse generate(GenerationRequest request);
}
