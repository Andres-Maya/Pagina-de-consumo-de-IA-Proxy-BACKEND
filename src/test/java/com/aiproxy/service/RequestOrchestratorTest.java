package com.aiproxy.service;

import com.aiproxy.model.GenerationRequest;
import com.aiproxy.model.GenerationResponse;
import com.aiproxy.model.SubscriptionPlan;
import com.aiproxy.repository.HistoryRepository;
import com.aiproxy.repository.PlanRepository;
import com.aiproxy.repository.UsageRepository;
import com.aiproxy.service.validation.QuotaValidator;
import com.aiproxy.service.validation.RateLimitValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RequestOrchestrator.
 * Tests the validation chain, short-circuit behavior, and usage recording.
 */
@ExtendWith(MockitoExtension.class)
class RequestOrchestratorTest {
    
    @Mock
    private PlanRepository planRepository;
    
    @Mock
    private UsageRepository usageRepository;
    
    @Mock
    private TextGenerationService textGenerator;
    
    @Mock
    private HistoryRepository historyRepository;
    
    private RateLimitValidator rateLimitValidator;
    private QuotaValidator quotaValidator;
    private RequestOrchestrator orchestrator;
    
    private GenerationRequest request;
    private String userId;
    
    @BeforeEach
    void setUp() {
        // Create real validators with mocked repositories
        rateLimitValidator = new RateLimitValidator(planRepository, usageRepository);
        quotaValidator = new QuotaValidator(planRepository, usageRepository);
        
        orchestrator = new RequestOrchestrator(
            rateLimitValidator,
            quotaValidator,
            textGenerator,
            historyRepository
        );
        
        userId = "user123";
        request = new GenerationRequest("Test prompt", 100, 0.7);
    }
    
    @Test
    void processRequest_whenAllValidationsPass_shouldGenerateAndRecordUsage() {
        // Arrange
        when(planRepository.getPlan(userId)).thenReturn(SubscriptionPlan.FREE);
        when(usageRepository.getRequestCount(userId)).thenReturn(5); // Within FREE limit of 10
        when(usageRepository.getMonthlyTokenUsage(userId)).thenReturn(1000); // Within FREE limit of 50,000
        
        GenerationResponse expectedResponse = new GenerationResponse("Generated text", 150, 1200);
        when(textGenerator.generate(request)).thenReturn(expectedResponse);
        
        // Act
        GenerationResponse response = orchestrator.processRequest(userId, request);
        
        // Assert
        assertNotNull(response);
        assertEquals("Generated text", response.getText());
        assertEquals(150, response.getTokensUsed());
        
        // Verify chain execution
        verify(usageRepository).incrementRequestCount(userId); // Rate limit validator
        verify(usageRepository).addTokenUsage(userId, 100); // Quota validator (request.getMaxTokens())
        verify(textGenerator).generate(request);
        verify(historyRepository).recordDailyUsage(eq(userId), eq(LocalDate.now()), eq(150));
    }
    
    @Test
    void processRequest_whenRateLimitExceeded_shouldThrowExceptionAndNotProceed() {
        // Arrange
        when(planRepository.getPlan(userId)).thenReturn(SubscriptionPlan.FREE);
        when(usageRepository.getRequestCount(userId)).thenReturn(10); // At FREE limit of 10
        
        // Act & Assert
        RequestOrchestrator.RateLimitException exception = assertThrows(
            RequestOrchestrator.RateLimitException.class,
            () -> orchestrator.processRequest(userId, request)
        );
        
        assertEquals("Rate limit exceeded", exception.getMessage());
        assertEquals(60, exception.getRetryAfterSeconds());
        
        // Verify short-circuit: quota validator and generator should not be called
        verify(usageRepository, never()).incrementRequestCount(userId);
        verify(usageRepository, never()).addTokenUsage(any(), anyInt());
        verify(textGenerator, never()).generate(any());
        verify(historyRepository, never()).recordDailyUsage(any(), any(), anyInt());
    }
    
    @Test
    void processRequest_whenQuotaExhausted_shouldThrowExceptionAndNotGenerate() {
        // Arrange
        when(planRepository.getPlan(userId)).thenReturn(SubscriptionPlan.FREE);
        when(usageRepository.getRequestCount(userId)).thenReturn(5); // Within rate limit
        when(usageRepository.getMonthlyTokenUsage(userId)).thenReturn(49950); // 49950 + 100 > 50000
        
        // Act & Assert
        RequestOrchestrator.QuotaExhaustedException exception = assertThrows(
            RequestOrchestrator.QuotaExhaustedException.class,
            () -> orchestrator.processRequest(userId, request)
        );
        
        assertEquals("Monthly quota exhausted", exception.getMessage());
        
        // Verify short-circuit: generator should not be called
        verify(usageRepository).incrementRequestCount(userId); // Rate limit passed
        verify(usageRepository, never()).addTokenUsage(any(), anyInt()); // Quota rejected before deduction
        verify(textGenerator, never()).generate(any());
        verify(historyRepository, never()).recordDailyUsage(any(), any(), anyInt());
    }
    
    @Test
    void processRequest_shouldInvokeValidatorsInCorrectOrder() {
        // Arrange
        when(planRepository.getPlan(userId)).thenReturn(SubscriptionPlan.PRO);
        when(usageRepository.getRequestCount(userId)).thenReturn(30);
        when(usageRepository.getMonthlyTokenUsage(userId)).thenReturn(10000);
        when(textGenerator.generate(request))
            .thenReturn(new GenerationResponse("Text", 100, 1200));
        
        // Act
        orchestrator.processRequest(userId, request);
        
        // Assert - verify order using InOrder
        var inOrder = inOrder(usageRepository, textGenerator, historyRepository);
        inOrder.verify(usageRepository).incrementRequestCount(userId); // Rate limit check
        inOrder.verify(usageRepository).addTokenUsage(userId, 100); // Quota check
        inOrder.verify(textGenerator).generate(request);
        inOrder.verify(historyRepository).recordDailyUsage(any(), any(), anyInt());
    }
    
    @Test
    void processRequest_shouldRecordCorrectTokenUsageInHistory() {
        // Arrange
        when(planRepository.getPlan(userId)).thenReturn(SubscriptionPlan.ENTERPRISE);
        
        int expectedTokens = 250;
        when(textGenerator.generate(request))
            .thenReturn(new GenerationResponse("Text", expectedTokens, 1200));
        
        // Act
        orchestrator.processRequest(userId, request);
        
        // Assert
        verify(historyRepository).recordDailyUsage(
            eq(userId),
            eq(LocalDate.now()),
            eq(expectedTokens)
        );
    }
    
    @Test
    void processRequest_whenEnterpriseUserUnlimitedRequests_shouldAlwaysPass() {
        // Arrange
        when(planRepository.getPlan(userId)).thenReturn(SubscriptionPlan.ENTERPRISE);
        when(textGenerator.generate(request))
            .thenReturn(new GenerationResponse("Text", 100, 1200));
        
        // Act
        GenerationResponse response = orchestrator.processRequest(userId, request);
        
        // Assert
        assertNotNull(response);
        verify(usageRepository).incrementRequestCount(userId);
        verify(textGenerator).generate(request);
        verify(historyRepository).recordDailyUsage(any(), any(), anyInt());
    }
}
