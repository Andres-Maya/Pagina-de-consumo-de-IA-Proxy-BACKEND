package com.aiproxy.service;

import com.aiproxy.model.GenerationRequest;
import com.aiproxy.model.GenerationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SimulatedGenerator.
 * Validates simulated processing delay, response generation, and token calculation.
 */
class SimulatedGeneratorTest {
    
    private SimulatedGenerator generator;
    
    @BeforeEach
    void setUp() {
        generator = new SimulatedGenerator();
    }
    
    @Test
    void testGenerateReturnsNonNullResponse() {
        // Given
        GenerationRequest request = new GenerationRequest("Test prompt", 100, 0.7);
        
        // When
        GenerationResponse response = generator.generate(request);
        
        // Then
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getText(), "Response text should not be null");
        assertFalse(response.getText().isEmpty(), "Response text should not be empty");
    }
    
    @Test
    void testGenerateSimulatesProcessingDelay() {
        // Given
        GenerationRequest request = new GenerationRequest("Test prompt", 100, 0.7);
        long expectedDelayMs = 1200;
        long tolerance = 100; // Allow 100ms tolerance
        
        // When
        long startTime = System.currentTimeMillis();
        GenerationResponse response = generator.generate(request);
        long actualDelay = System.currentTimeMillis() - startTime;
        
        // Then
        assertTrue(actualDelay >= expectedDelayMs, 
            "Processing should take at least " + expectedDelayMs + "ms, but took " + actualDelay + "ms");
        assertTrue(actualDelay <= expectedDelayMs + tolerance, 
            "Processing should take at most " + (expectedDelayMs + tolerance) + "ms, but took " + actualDelay + "ms");
        assertTrue(response.getProcessingTimeMs() >= expectedDelayMs,
            "Reported processing time should be at least " + expectedDelayMs + "ms");
    }
    
    @Test
    void testGenerateReturnsTokensInValidRange() {
        // Given
        GenerationRequest request = new GenerationRequest("Test prompt", 100, 0.7);
        int minTokens = 100;
        int maxTokens = 500;
        
        // When
        GenerationResponse response = generator.generate(request);
        
        // Then
        assertTrue(response.getTokensUsed() >= minTokens, 
            "Tokens used should be at least " + minTokens + ", but was " + response.getTokensUsed());
        assertTrue(response.getTokensUsed() <= maxTokens, 
            "Tokens used should be at most " + maxTokens + ", but was " + response.getTokensUsed());
    }
    
    @Test
    void testGenerateReturnsVariedResponses() {
        // Given
        GenerationRequest request = new GenerationRequest("Test prompt", 100, 0.7);
        
        // When - Generate multiple responses
        String firstResponse = generator.generate(request).getText();
        String secondResponse = generator.generate(request).getText();
        String thirdResponse = generator.generate(request).getText();
        
        // Then - At least one should be different (with high probability)
        boolean hasVariation = !firstResponse.equals(secondResponse) || 
                              !secondResponse.equals(thirdResponse);
        assertTrue(hasVariation, "Generator should return varied responses");
    }
    
    @Test
    void testGenerateImplementsTextGenerationService() {
        // Then
        assertTrue(generator instanceof TextGenerationService, 
            "SimulatedGenerator should implement TextGenerationService interface");
    }
}
