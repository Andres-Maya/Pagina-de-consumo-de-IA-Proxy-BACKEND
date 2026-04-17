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
class QuotaValidatorTest {
    
    @Mock
    private PlanRepository planRepository;
    
    @Mock
    private UsageRepository usageRepository;
    
    private QuotaValidator validator;
    
    private GenerationRequest request;
    
    @BeforeEach
    void setUp() {
        validator = new QuotaValidator(planRepository, usageRepository);
        request = new GenerationRequest();
        request.setPrompt("Test prompt");
        request.setMaxTokens(100);
        request.setTemperature(0.7);
    }
    
    @Test
    void shouldApproveFreeUserWithinQuota() {
        // Given
        String userId = "user1";
        when(planRepository.getPlan(userId)).thenReturn(SubscriptionPlan.FREE);
        when(usageRepository.getMonthlyTokenUsage(userId)).thenReturn(10_000);
        
        // When
        ValidationResult result = validator.validate(userId, request);
        
        // Then
        assertTrue(result.isApproved());
        assertNull(result.getRejectionReason());
        assertNull(result.getRetryAfterSeconds());
        verify(usageRepository).addTokenUsage(userId, 100);
    }
    
    @Test
    void shouldRejectFreeUserAtQuotaLimit() {
        // Given
        String userId = "user1";
        when(planRepository.getPlan(userId)).thenReturn(SubscriptionPlan.FREE);
        when(usageRepository.getMonthlyTokenUsage(userId)).thenReturn(49_950);
        request.setMaxTokens(100);
        
        // When
        ValidationResult result = validator.validate(userId, request);
        
        // Then
        assertFalse(result.isApproved());
        assertEquals("Monthly quota exhausted", result.getRejectionReason());
        assertNull(result.getRetryAfterSeconds());
        verify(usageRepository, never()).addTokenUsage(anyString(), anyInt());
    }
    
    @Test
    void shouldRejectFreeUserExceedingQuota() {
        // Given
        String userId = "user1";
        when(planRepository.getPlan(userId)).thenReturn(SubscriptionPlan.FREE);
        when(usageRepository.getMonthlyTokenUsage(userId)).thenReturn(50_000);
        
        // When
        ValidationResult result = validator.validate(userId, request);
        
        // Then
        assertFalse(result.isApproved());
        assertEquals("Monthly quota exhausted", result.getRejectionReason());
        verify(usageRepository, never()).addTokenUsage(anyString(), anyInt());
    }
    
    @Test
    void shouldApproveProUserWithinQuota() {
        // Given
        String userId = "user2";
        when(planRepository.getPlan(userId)).thenReturn(SubscriptionPlan.PRO);
        when(usageRepository.getMonthlyTokenUsage(userId)).thenReturn(250_000);
        request.setMaxTokens(1000);
        
        // When
        ValidationResult result = validator.validate(userId, request);
        
        // Then
        assertTrue(result.isApproved());
        assertNull(result.getRejectionReason());
        verify(usageRepository).addTokenUsage(userId, 1000);
    }
    
    @Test
    void shouldRejectProUserAtQuotaLimit() {
        // Given
        String userId = "user2";
        when(planRepository.getPlan(userId)).thenReturn(SubscriptionPlan.PRO);
        when(usageRepository.getMonthlyTokenUsage(userId)).thenReturn(499_500);
        request.setMaxTokens(1000);
        
        // When
        ValidationResult result = validator.validate(userId, request);
        
        // Then
        assertFalse(result.isApproved());
        assertEquals("Monthly quota exhausted", result.getRejectionReason());
        verify(usageRepository, never()).addTokenUsage(anyString(), anyInt());
    }
    
    @Test
    void shouldApproveEnterpriseUserAlways() {
        // Given
        String userId = "user3";
        when(planRepository.getPlan(userId)).thenReturn(SubscriptionPlan.ENTERPRISE);
        request.setMaxTokens(1_000_000);
        
        // When
        ValidationResult result = validator.validate(userId, request);
        
        // Then
        assertTrue(result.isApproved());
        assertNull(result.getRejectionReason());
        assertNull(result.getRetryAfterSeconds());
        verify(usageRepository, never()).addTokenUsage(anyString(), anyInt());
        verify(usageRepository, never()).getMonthlyTokenUsage(userId);
    }
    
    @Test
    void shouldApproveFreeUserAtZeroUsage() {
        // Given
        String userId = "user1";
        when(planRepository.getPlan(userId)).thenReturn(SubscriptionPlan.FREE);
        when(usageRepository.getMonthlyTokenUsage(userId)).thenReturn(0);
        
        // When
        ValidationResult result = validator.validate(userId, request);
        
        // Then
        assertTrue(result.isApproved());
        verify(usageRepository).addTokenUsage(userId, 100);
    }
    
    @Test
    void shouldApproveFreeUserJustBelowLimit() {
        // Given
        String userId = "user1";
        when(planRepository.getPlan(userId)).thenReturn(SubscriptionPlan.FREE);
        when(usageRepository.getMonthlyTokenUsage(userId)).thenReturn(49_900);
        
        // When
        ValidationResult result = validator.validate(userId, request);
        
        // Then
        assertTrue(result.isApproved());
        verify(usageRepository).addTokenUsage(userId, 100);
    }
    
    @Test
    void shouldDeductExactRequestedTokens() {
        // Given
        String userId = "user1";
        when(planRepository.getPlan(userId)).thenReturn(SubscriptionPlan.FREE);
        when(usageRepository.getMonthlyTokenUsage(userId)).thenReturn(1000);
        request.setMaxTokens(500);
        
        // When
        ValidationResult result = validator.validate(userId, request);
        
        // Then
        assertTrue(result.isApproved());
        verify(usageRepository).addTokenUsage(userId, 500);
    }
    
    @Test
    void shouldRejectWhenExactlyAtLimit() {
        // Given
        String userId = "user1";
        when(planRepository.getPlan(userId)).thenReturn(SubscriptionPlan.FREE);
        when(usageRepository.getMonthlyTokenUsage(userId)).thenReturn(50_000);
        request.setMaxTokens(1);
        
        // When
        ValidationResult result = validator.validate(userId, request);
        
        // Then
        assertFalse(result.isApproved());
        assertEquals("Monthly quota exhausted", result.getRejectionReason());
        verify(usageRepository, never()).addTokenUsage(anyString(), anyInt());
    }
}
