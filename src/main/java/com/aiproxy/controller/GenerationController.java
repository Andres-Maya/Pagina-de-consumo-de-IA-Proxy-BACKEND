package com.aiproxy.controller;

import com.aiproxy.model.GenerationRequest;
import com.aiproxy.model.GenerationResponse;
import com.aiproxy.service.RequestOrchestrator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for AI text generation endpoints.
 * Handles HTTP requests for text generation and delegates to RequestOrchestrator.
 * 
 * Endpoints:
 * - POST /api/ai/generate: Submit a text generation request
 * 
 * HTTP Status Codes:
 * - 200 OK: Generation successful
 * - 429 Too Many Requests: Rate limit exceeded
 * - 402 Payment Required: Monthly quota exhausted
 * - 400 Bad Request: Invalid request parameters
 */
@RestController
@RequestMapping("/api/ai")
public class GenerationController {
    
    private final RequestOrchestrator orchestrator;
    
    public GenerationController(RequestOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }
    
    /**
     * Generates AI text based on the provided prompt and parameters.
     * 
     * @param userId the unique identifier of the user (from User-Id header)
     * @param request the generation request containing prompt and parameters
     * @return ResponseEntity with GenerationResponse and appropriate HTTP status
     */
    @PostMapping("/generate")
    public ResponseEntity<GenerationResponse> generate(
            @RequestHeader("User-Id") String userId,
            @Valid @RequestBody GenerationRequest request) {
        
        try {
            GenerationResponse response = orchestrator.processRequest(userId, request);
            return ResponseEntity.ok(response);
            
        } catch (RequestOrchestrator.RateLimitException e) {
            // Rate limit exceeded - return 429 with Retry-After header
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("Retry-After", String.valueOf(e.getRetryAfterSeconds()))
                    .build();
                    
        } catch (RequestOrchestrator.QuotaExhaustedException e) {
            // Quota exhausted - return 402 Payment Required
            return ResponseEntity
                    .status(HttpStatus.PAYMENT_REQUIRED)
                    .build();
        }
    }
}
