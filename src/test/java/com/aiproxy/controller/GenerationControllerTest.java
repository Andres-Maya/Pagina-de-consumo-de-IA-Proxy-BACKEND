package com.aiproxy.controller;

import com.aiproxy.model.GenerationRequest;
import com.aiproxy.model.GenerationResponse;
import com.aiproxy.repository.HistoryRepository;
import com.aiproxy.service.RequestOrchestrator;
import com.aiproxy.service.TextGenerationService;
import com.aiproxy.service.validation.QuotaValidator;
import com.aiproxy.service.validation.RateLimitValidator;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GenerationController.
 * Tests the HTTP layer behavior and status code mapping.
 */
class GenerationControllerTest {
    
    /**
     * Stub orchestrator that returns a successful response
     */
    private static class SuccessOrchestrator extends RequestOrchestrator {
        private final GenerationResponse response;
        
        public SuccessOrchestrator(GenerationResponse response) {
            super(null, null, null, null);
            this.response = response;
        }
        
        @Override
        public GenerationResponse processRequest(String userId, GenerationRequest request) {
            return response;
        }
    }
    
    /**
     * Stub orchestrator that throws RateLimitException
     */
    private static class RateLimitOrchestrator extends RequestOrchestrator {
        private final int retryAfter;
        
        public RateLimitOrchestrator(int retryAfter) {
            super(null, null, null, null);
            this.retryAfter = retryAfter;
        }
        
        @Override
        public GenerationResponse processRequest(String userId, GenerationRequest request) {
            throw new RateLimitException("Rate limit exceeded", retryAfter);
        }
    }
    
    /**
     * Stub orchestrator that throws QuotaExhaustedException
     */
    private static class QuotaExhaustedOrchestrator extends RequestOrchestrator {
        public QuotaExhaustedOrchestrator() {
            super(null, null, null, null);
        }
        
        @Override
        public GenerationResponse processRequest(String userId, GenerationRequest request) {
            throw new QuotaExhaustedException("Monthly quota exhausted");
        }
    }
    
    @Test
    void testGenerateSuccess() {
        // Arrange
        String userId = "user123";
        GenerationRequest request = new GenerationRequest("Test prompt", 100, 0.7);
        GenerationResponse expectedResponse = new GenerationResponse("Generated text", 50, 1200);
        
        RequestOrchestrator orchestrator = new SuccessOrchestrator(expectedResponse);
        GenerationController controller = new GenerationController(orchestrator);
        
        // Act
        ResponseEntity<GenerationResponse> response = controller.generate(userId, request);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Generated text", response.getBody().getText());
        assertEquals(50, response.getBody().getTokensUsed());
    }
    
    @Test
    void testGenerateRateLimitExceeded() {
        // Arrange
        String userId = "user123";
        GenerationRequest request = new GenerationRequest("Test prompt", 100, 0.7);
        
        RequestOrchestrator orchestrator = new RateLimitOrchestrator(45);
        GenerationController controller = new GenerationController(orchestrator);
        
        // Act
        ResponseEntity<GenerationResponse> response = controller.generate(userId, request);
        
        // Assert
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals("45", response.getHeaders().getFirst("Retry-After"));
        assertNull(response.getBody());
    }
    
    @Test
    void testGenerateQuotaExhausted() {
        // Arrange
        String userId = "user123";
        GenerationRequest request = new GenerationRequest("Test prompt", 100, 0.7);
        
        RequestOrchestrator orchestrator = new QuotaExhaustedOrchestrator();
        GenerationController controller = new GenerationController(orchestrator);
        
        // Act
        ResponseEntity<GenerationResponse> response = controller.generate(userId, request);
        
        // Assert
        assertEquals(HttpStatus.PAYMENT_REQUIRED, response.getStatusCode());
        assertNull(response.getBody());
    }
    
    @Test
    void testGenerateWithDifferentUserId() {
        // Arrange
        String userId = "user456";
        GenerationRequest request = new GenerationRequest("Another prompt", 200, 1.0);
        GenerationResponse expectedResponse = new GenerationResponse("Different text", 100, 1200);
        
        RequestOrchestrator orchestrator = new SuccessOrchestrator(expectedResponse);
        GenerationController controller = new GenerationController(orchestrator);
        
        // Act
        ResponseEntity<GenerationResponse> response = controller.generate(userId, request);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Different text", response.getBody().getText());
    }
    
    @Test
    void testGenerateRetryAfterHeaderPresent() {
        // Arrange
        String userId = "user123";
        GenerationRequest request = new GenerationRequest("Test prompt", 100, 0.7);
        
        RequestOrchestrator orchestrator = new RateLimitOrchestrator(30);
        GenerationController controller = new GenerationController(orchestrator);
        
        // Act
        ResponseEntity<GenerationResponse> response = controller.generate(userId, request);
        
        // Assert
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertTrue(response.getHeaders().containsKey("Retry-After"));
        assertEquals("30", response.getHeaders().getFirst("Retry-After"));
    }
}
