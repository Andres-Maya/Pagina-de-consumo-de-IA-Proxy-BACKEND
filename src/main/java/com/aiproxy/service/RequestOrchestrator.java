package com.aiproxy.service;

import com.aiproxy.model.GenerationRequest;
import com.aiproxy.model.GenerationResponse;
import com.aiproxy.model.ValidationResult;
import com.aiproxy.repository.HistoryRepository;
import com.aiproxy.service.validation.QuotaValidator;
import com.aiproxy.service.validation.RateLimitValidator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Orchestrates the request validation chain and generation process.
 * Implements the proxy chain pattern by coordinating validators and the text generator.
 * 
 * Request flow:
 * 1. RateLimitValidator - checks per-minute request limits
 * 2. QuotaValidator - checks monthly token quotas
 * 3. TextGenerationService - generates the AI response
 * 4. HistoryRepository - records usage on success
 * 
 * The chain short-circuits on the first rejection, returning immediately
 * without invoking subsequent components.
 */
@Service
public class RequestOrchestrator {
    
    private final RateLimitValidator rateLimitValidator;
    private final QuotaValidator quotaValidator;
    private final TextGenerationService textGenerator;
    private final HistoryRepository historyRepository;
    
    public RequestOrchestrator(
            RateLimitValidator rateLimitValidator,
            QuotaValidator quotaValidator,
            TextGenerationService textGenerator,
            HistoryRepository historyRepository) {
        this.rateLimitValidator = rateLimitValidator;
        this.quotaValidator = quotaValidator;
        this.textGenerator = textGenerator;
        this.historyRepository = historyRepository;
    }
    
    /**
     * Processes a generation request through the validation chain.
     * 
     * @param userId the unique identifier of the user making the request
     * @param request the generation request with prompt and parameters
     * @return the generation response if all validations pass
     * @throws RateLimitException if rate limit is exceeded
     * @throws QuotaExhaustedException if monthly quota is insufficient
     */
    public GenerationResponse processRequest(String userId, GenerationRequest request) {
        // Step 1: Rate limit validation
        ValidationResult rateLimitResult = rateLimitValidator.validate(userId, request);
        if (!rateLimitResult.isApproved()) {
            throw new RateLimitException(
                rateLimitResult.getRejectionReason(),
                rateLimitResult.getRetryAfterSeconds()
            );
        }
        
        // Step 2: Quota validation
        ValidationResult quotaResult = quotaValidator.validate(userId, request);
        if (!quotaResult.isApproved()) {
            throw new QuotaExhaustedException(quotaResult.getRejectionReason());
        }
        
        // Step 3: Generate text
        GenerationResponse response = textGenerator.generate(request);
        
        // Step 4: Record usage in history
        historyRepository.recordDailyUsage(userId, LocalDate.now(), response.getTokensUsed());
        
        return response;
    }
    
    /**
     * Exception thrown when rate limit is exceeded.
     */
    public static class RateLimitException extends RuntimeException {
        private final Integer retryAfterSeconds;
        
        public RateLimitException(String message, Integer retryAfterSeconds) {
            super(message);
            this.retryAfterSeconds = retryAfterSeconds;
        }
        
        public Integer getRetryAfterSeconds() {
            return retryAfterSeconds;
        }
    }
    
    /**
     * Exception thrown when monthly quota is exhausted.
     */
    public static class QuotaExhaustedException extends RuntimeException {
        public QuotaExhaustedException(String message) {
            super(message);
        }
    }
}
