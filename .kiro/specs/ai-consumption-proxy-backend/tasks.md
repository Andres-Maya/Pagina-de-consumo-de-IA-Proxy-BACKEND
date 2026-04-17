# Implementation Plan: AI Consumption Proxy Backend

## Overview

This plan implements a minimal viable Spring Boot backend with simulated AI text generation, rate limiting, and quota management. The focus is on core functionality that can be completed in ONE HOUR using in-memory storage and essential property tests only.

**Time Constraint**: All tasks designed for 1-hour total completion
**Approach**: Minimal implementations, in-memory storage, 3 critical property tests only

## Tasks

- [x] 1. Bootstrap Spring Boot project structure
  - Create Maven project with Spring Boot 3.x and Java 17
  - Add dependencies: spring-boot-starter-web, spring-boot-starter-actuator, lombok, jqwik (test)
  - Create package structure: controller, service, repository, model
  - Configure application.yml with PORT environment variable binding
  - _Requirements: 14.1, 14.2, 14.3, 12.3_

- [ ] 2. Implement core data models
  - [x] 2.1 Create request/response POJOs
    - GenerationRequest (prompt, maxTokens, temperature)
    - GenerationResponse (text, tokensUsed, processingTimeMs)
    - QuotaStatus (tokensUsed, tokensRemaining, resetDate, currentPlan)
    - DailyUsage (date, tokensUsed)
    - PlanDetails (plan, requestsPerMinute, monthlyTokens)
    - _Requirements: 1.1, 6.1, 7.2, 7.3, 7.4, 7.5, 8.3, 9.5_
  
  - [x] 2.2 Create domain models
    - SubscriptionPlan enum (FREE=10/50K, PRO=60/500K, ENTERPRISE=unlimited)
    - ValidationResult (approved, rejectionReason, retryAfterSeconds)
    - _Requirements: 3.1, 3.2, 3.3, 4.1, 4.2, 4.3_

- [ ] 3. Implement in-memory repositories
  - [x] 3.1 Create UsageRepository with ConcurrentHashMap
    - Track request counts per user (rate limiting)
    - Track monthly token usage per user (quota)
    - Methods: incrementRequestCount, getRequestCount, resetRequestCounts, addTokenUsage, getMonthlyTokenUsage, resetMonthlyUsage
    - _Requirements: 3.1, 3.2, 3.3, 4.4, 10.2, 11.2_
  
  - [x] 3.2 Create PlanRepository with ConcurrentHashMap
    - Store user subscription plans (default all users to FREE)
    - Methods: getPlan, updatePlan
    - _Requirements: 9.2, 9.3_
  
  - [-] 3.3 Create HistoryRepository with ConcurrentHashMap
    - Store daily usage by user and date
    - Methods: recordDailyUsage, getDailyUsage (7 days)
    - _Requirements: 8.2, 8.3, 8.4_

- [ ] 4. Implement text generation service
  - [~] 4.1 Create TextGenerationService interface
    - Define generate(GenerationRequest) method
    - _Requirements: 1.1, 1.2, 1.3_
  
  - [~] 4.2 Implement SimulatedGenerator
    - Sleep for 1200ms to simulate processing
    - Return random predefined text response
    - Calculate tokensUsed (random 100-500)
    - _Requirements: 2.1, 2.2, 2.3_

- [ ] 5. Implement validation chain
  - [~] 5.1 Create RequestValidator interface
    - Define validate(userId, request) method returning ValidationResult
    - _Requirements: 5.1, 5.2, 5.3_
  
  - [~] 5.2 Implement RateLimitValidator
    - Check user's plan from PlanRepository
    - Check current minute's request count from UsageRepository
    - Enforce plan limits: FREE=10, PRO=60, ENTERPRISE=unlimited
    - Return 429 with retryAfter if exceeded, otherwise approve
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_
  
  - [~] 5.3 Implement QuotaValidator
    - Check user's plan from PlanRepository
    - Check monthly token usage from UsageRepository
    - Enforce plan quotas: FREE=50K, PRO=500K, ENTERPRISE=unlimited
    - Deduct tokens if sufficient, return 402 if insufficient
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6_
  
  - [~] 5.4 Implement RequestOrchestrator
    - Chain validators: RateLimitValidator → QuotaValidator → TextGenerationService
    - Short-circuit on first rejection
    - Record usage in HistoryRepository on success
    - _Requirements: 5.1, 5.2, 5.3, 5.4_

- [ ] 6. Implement REST controllers
  - [~] 6.1 Create GenerationController
    - POST /api/ai/generate endpoint
    - Extract User-Id from header
    - Call RequestOrchestrator
    - Return 200/429/402 based on result
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_
  
  - [~] 6.2 Create QuotaController
    - GET /api/quota/status endpoint (return QuotaStatus)
    - GET /api/quota/history endpoint (return 7-day DailyUsage list)
    - POST /api/quota/upgrade endpoint (call PlanManager)
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 8.1, 8.2, 8.3, 8.4, 9.1_

- [ ] 7. Implement supporting services
  - [~] 7.1 Create UsageTracker
    - recordUsage(userId, tokensUsed) - updates HistoryRepository
    - getHistory(userId, 7) - retrieves 7-day history with zero-fill
    - _Requirements: 8.2, 8.3, 8.4_
  
  - [~] 7.2 Create PlanManager
    - upgradePlan(userId) - FREE→PRO→ENTERPRISE, reject at max
    - getCurrentPlan(userId) - retrieve from PlanRepository
    - _Requirements: 9.2, 9.3, 9.4, 9.5_
  
  - [~] 7.3 Create LimitResetter with @Scheduled tasks
    - @Scheduled(cron = "0 * * * * *") resetRateLimits() - every minute
    - @Scheduled(cron = "0 0 0 1 * *") resetMonthlyQuotas() - first of month
    - _Requirements: 10.1, 10.2, 10.3, 11.1, 11.2, 11.3_

- [~] 8. Checkpoint - Verify core functionality
  - Run application locally with `./mvnw spring-boot:run`
  - Test POST /api/ai/generate with curl (should return 200 with generated text)
  - Test GET /api/quota/status (should return quota info)
  - Ensure all tests pass, ask the user if questions arise.

- [ ]* 9. Write critical property-based tests (3 only)
  - [ ]* 9.1 Property test for rate limiting enforcement
    - **Property 3: Plan-Specific Rate Limiting**
    - **Validates: Requirements 3.1, 3.2, 3.3**
    - Generate random request sequences for each plan type
    - Verify FREE allows exactly 10/min, PRO allows 60/min, ENTERPRISE unlimited
  
  - [ ]* 9.2 Property test for quota enforcement
    - **Property 6: Plan-Specific Quota Enforcement**
    - **Validates: Requirements 4.1, 4.2, 4.3**
    - Generate random token usage patterns for each plan type
    - Verify FREE allows 50K, PRO allows 500K, ENTERPRISE unlimited
  
  - [ ]* 9.3 Property test for request chain ordering
    - **Property 10: Request Chain Ordering**
    - **Validates: Requirements 5.1, 5.2, 5.3**
    - Generate random request scenarios
    - Verify validators invoked in correct order: RateLimit → Quota → Generator

- [ ] 10. Create deployment configuration
  - [~] 10.1 Create Dockerfile
    - Multi-stage build with Maven
    - Use eclipse-temurin:17-jdk-alpine for build
    - Use eclipse-temurin:17-jre-alpine for runtime
    - Expose PORT environment variable
    - ENTRYPOINT with -Dserver.port=${PORT:-8080}
    - _Requirements: 12.1, 12.3_
  
  - [~] 10.2 Create render.yaml
    - Configure web service with docker environment
    - Set healthCheckPath to /actuator/health
    - Configure environment variables: SPRING_PROFILES_ACTIVE=production
    - _Requirements: 12.4_

- [~] 11. Final checkpoint - Verify deployment readiness
  - Build Docker image: `docker build -t ai-proxy .`
  - Run Docker container: `docker run -p 8080:8080 -e PORT=8080 ai-proxy`
  - Test health endpoint: `curl http://localhost:8080/actuator/health`
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional property tests (can be skipped for faster MVP)
- Focus is on MINIMAL working implementation within 1-hour constraint
- In-memory storage only (ConcurrentHashMap) - no database setup
- Only 3 critical property tests instead of all 17 (time constraint)
- Skipped: extensive error handling, integration tests, advanced features
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation before proceeding
