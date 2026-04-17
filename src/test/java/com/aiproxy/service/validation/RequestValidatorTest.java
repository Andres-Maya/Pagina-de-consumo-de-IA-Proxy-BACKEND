package com.aiproxy.service.validation;

import com.aiproxy.model.GenerationRequest;
import com.aiproxy.model.ValidationResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RequestValidator interface contract.
 * Tests verify that implementations follow the expected behavior.
 */
class RequestValidatorTest {
    
    @Test
    void testValidatorInterfaceContract() {
        // Create a mock implementation to verify interface contract
        RequestValidator validator = new RequestValidator() {
            @Override
            public ValidationResult validate(String userId, GenerationRequest request) {
                return ValidationResult.approved();
            }
        };
        
        GenerationRequest request = new GenerationRequest("Test prompt", 100, 0.7);
        ValidationResult result = validator.validate("user123", request);
        
        assertNotNull(result, "Validation result should not be null");
        assertTrue(result.isApproved(), "Mock validator should approve request");
    }
    
    @Test
    void testValidatorCanRejectRequests() {
        // Create a mock implementation that rejects requests
        RequestValidator validator = new RequestValidator() {
            @Override
            public ValidationResult validate(String userId, GenerationRequest request) {
                return ValidationResult.rejected("Test rejection");
            }
        };
        
        GenerationRequest request = new GenerationRequest("Test prompt", 100, 0.7);
        ValidationResult result = validator.validate("user123", request);
        
        assertNotNull(result, "Validation result should not be null");
        assertFalse(result.isApproved(), "Mock validator should reject request");
        assertEquals("Test rejection", result.getRejectionReason(), 
                "Rejection reason should match");
    }
    
    @Test
    void testValidatorCanReturnRateLimitResult() {
        // Create a mock implementation that returns rate limit result
        RequestValidator validator = new RequestValidator() {
            @Override
            public ValidationResult validate(String userId, GenerationRequest request) {
                return ValidationResult.rateLimited(60);
            }
        };
        
        GenerationRequest request = new GenerationRequest("Test prompt", 100, 0.7);
        ValidationResult result = validator.validate("user123", request);
        
        assertNotNull(result, "Validation result should not be null");
        assertFalse(result.isApproved(), "Rate limited request should not be approved");
        assertEquals("Rate limit exceeded", result.getRejectionReason(), 
                "Rejection reason should indicate rate limit");
        assertEquals(60, result.getRetryAfterSeconds(), 
                "Retry after should be 60 seconds");
    }
}
