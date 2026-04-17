package com.aiproxy.service.validation;

import com.aiproxy.model.GenerationRequest;
import com.aiproxy.model.SubscriptionPlan;
import com.aiproxy.model.ValidationResult;
import com.aiproxy.repository.PlanRepository;
import com.aiproxy.repository.UsageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitValidatorTest {
    
    @Mock
    private PlanRepository planRepository;
    
    @Mock
    private UsageRepository usageRepository;
    
    private RateLimitValidator validator;
    
    private GenerationRequest request;
    
    @BeforeEach
    void setUp() {
        validator = new RateLimitValidator(planRepository, usageRepository);
        request = new GenerationRequest();
        request.setPrompt("Test prompt");
        request.setMaxTokens(100);
    }
    
    @Test
    void shouldApproveFreeUserWithinLimit() {
        // Given
        String userId = "user1";
        when(planRepository.getPlan(userId)).thenReturn(SubscriptionPlan.FREE);
        when(usageRepository.getRequestCount(userId)).thenReturn(5);
        
        // When
        ValidationResult result = validator.validate(userId, request);
        
        // Then
        assertTrue(result.isApproved());
        assertNull(result.getRejectionReason());
        assertNull(result.getRetryAfterSeconds());
        verify(usageRepository).incrementRequestCount(userId);
    }
    
    @Test
    void shouldRejectFreeUserAtLimit() {
        // Given
        String userId = "user1";
        when(planRepository.getPlan(userId)).thenReturn(SubscriptionPlan.FREE);
        when(usageRepository.getRequestCount(userId)).thenReturn(10);
        
        // When
        ValidationResult result = validator.validate(userId, request);
        
        // Then
        assertFalse(result.isApproved());
        assertEquals("Rate limit exceeded", result.getRejectionReason());
        assertEquals(60, result.getRetryAfterSeconds());
        verify(usageRepository, never()).incrementRequestCount(userId);
    }
    
    @Test
    void shouldRejectFreeUserExceedingLimit() {
        // Given
        String userId = "user1";
        when(planRepository.getPlan(userId)).thenReturn(SubscriptionPlan.FREE);
        when(usageRepository.getRequestCount(userId)).thenReturn(15);
        
        // When
        ValidationResult result = validator.validate(userId, request);
        
        // Then
        assertFalse(result.isApproved());
        assertEquals("Rate limit exceeded", result.getRejectionReason());
        assertEquals(60, result.getRetryAfterSeconds());
        verify(usageRepository, never()).incrementRequestCount(userId);
    }
    
    @Test
    void shouldApproveProUserWithinLimit() {
        // Given
        String userId = "user2";
        when(planRepository.getPlan(userId)).thenReturn(SubscriptionPlan.PRO);
        when(usageRepository.getRequestCount(userId)).thenReturn(30);
        
        // When
        ValidationResult result = validator.validate(userId, request);
        
        // Then
        assertTrue(result.isApproved());
        assertNull(result.getRejectionReason());
        assertNull(result.getRetryAfterSeconds());
        verify(usageRepository).incrementRequestCount(userId);
    }
    
    @Test
    void shouldRejectProUserAtLimit() {
        // Given
        String userId = "user2";
        when(planRepository.getPlan(userId)).thenReturn(SubscriptionPlan.PRO);
        when(usageRepository.getRequestCount(userId)).thenReturn(60);
        
        // When
        ValidationResult result = validator.validate(userId, request);
        
        // Then
        assertFalse(result.isApproved());
        assertEquals("Rate limit exceeded", result.getRejectionReason());
        assertEquals(60, result.getRetryAfterSeconds());
        verify(usageRepository, never()).incrementRequestCount(userId);
    }
    
    @Test
    void shouldApproveEnterpriseUserAlways() {
        // Given
        String userId = "user3";
        when(planRepository.getPlan(userId)).thenReturn(SubscriptionPlan.ENTERPRISE);
        
        // When
        ValidationResult result = validator.validate(userId, request);
        
        // Then
        assertTrue(result.isApproved());
        assertNull(result.getRejectionReason());
        assertNull(result.getRetryAfterSeconds());
        verify(usageRepository).incrementRequestCount(userId);
        verify(usageRepository, never()).getRequestCount(userId);
    }
    
    @Test
    void shouldApproveEnterpriseUserWithZeroCount() {
        // Given
        String userId = "user3";
        when(planRepository.getPlan(userId)).thenReturn(SubscriptionPlan.ENTERPRISE);
        
        // When
        ValidationResult result = validator.validate(userId, request);
        
        // Then
        assertTrue(result.isApproved());
        verify(usageRepository).incrementRequestCount(userId);
        verify(usageRepository, never()).getRequestCount(userId);
    }
    
    @Test
    void shouldApproveFreeUserAtZeroCount() {
        // Given
        String userId = "user1";
        when(planRepository.getPlan(userId)).thenReturn(SubscriptionPlan.FREE);
        when(usageRepository.getRequestCount(userId)).thenReturn(0);
        
        // When
        ValidationResult result = validator.validate(userId, request);
        
        // Then
        assertTrue(result.isApproved());
        verify(usageRepository).incrementRequestCount(userId);
    }
    
    @Test
    void shouldApproveFreeUserAtLimitMinusOne() {
        // Given
        String userId = "user1";
        when(planRepository.getPlan(userId)).thenReturn(SubscriptionPlan.FREE);
        when(usageRepository.getRequestCount(userId)).thenReturn(9);
        
        // When
        ValidationResult result = validator.validate(userId, request);
        
        // Then
        assertTrue(result.isApproved());
        verify(usageRepository).incrementRequestCount(userId);
    }
}
