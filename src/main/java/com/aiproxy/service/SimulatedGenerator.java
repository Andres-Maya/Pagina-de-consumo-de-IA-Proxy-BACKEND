package com.aiproxy.service;

import com.aiproxy.model.GenerationRequest;
import com.aiproxy.model.GenerationResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/**
 * Mock implementation of TextGenerationService for testing and development.
 * Simulates realistic processing delay and returns random predefined responses.
 * 
 * This implementation is useful for:
 * - Testing the system without external AI service dependencies
 * - Development and integration testing
 * - Demonstrating the proxy chain behavior
 */
@Service
public class SimulatedGenerator implements TextGenerationService {
    
    private static final long PROCESSING_DELAY_MS = 1200;
    private static final int MIN_TOKENS = 100;
    private static final int MAX_TOKENS = 500;
    
    private static final List<String> PREDEFINED_RESPONSES = List.of(
        "The quick brown fox jumps over the lazy dog. This is a simulated AI response for testing purposes.",
        "Artificial intelligence is transforming the way we interact with technology and solve complex problems.",
        "In a world of constant change, adaptability and continuous learning are essential skills for success.",
        "The future of software development lies in the seamless integration of AI-powered tools and human creativity.",
        "Data-driven decision making enables organizations to optimize processes and deliver better outcomes."
    );
    
    private final Random random = new Random();
    
    /**
     * Generates a simulated AI text response with realistic processing delay.
     * 
     * @param request the generation request (prompt is ignored in simulation)
     * @return a generation response with random text and token usage
     */
    @Override
    public GenerationResponse generate(GenerationRequest request) {
        long startTime = System.currentTimeMillis();
        
        // Simulate processing delay
        try {
            Thread.sleep(PROCESSING_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Simulated generation interrupted", e);
        }
        
        // Select random predefined response
        String text = PREDEFINED_RESPONSES.get(random.nextInt(PREDEFINED_RESPONSES.size()));
        
        // Calculate random token usage
        int tokensUsed = MIN_TOKENS + random.nextInt(MAX_TOKENS - MIN_TOKENS + 1);
        
        // Calculate actual processing time
        long processingTime = System.currentTimeMillis() - startTime;
        
        return new GenerationResponse(text, tokensUsed, processingTime);
    }
}
