# Design Document: AI Consumption Proxy Backend

## Overview

The AI Consumption Proxy Backend is a Spring Boot application that provides simulated AI text generation services with comprehensive usage control through a proxy chain architecture. The system enforces rate limiting and quota management based on subscription tiers (FREE, PRO, ENTERPRISE) while tracking usage history and providing subscription management capabilities.

### Core Design Principles

1. **Layered Architecture**: Clear separation between Controller, Service, and Repository layers
2. **Proxy Chain Pattern**: Sequential validation through Rate Limit → Quota → Generation Service
3. **Domain-Driven Design**: Business-focused naming that avoids pattern terminology
4. **SOLID Principles**: Single responsibility, open/closed, dependency inversion
5. **Dependency Injection**: Spring-managed components with constructor injection
6. **Testability**: Mock implementations and clear interfaces for testing

### Key Features

- Text generation API with simulated processing
- Per-plan rate limiting (10/60/unlimited requests per minute)
- Monthly token quotas (50K/500K/unlimited tokens)
- Usage tracking and history retrieval
- Subscription plan upgrades
- Automated limit resets (rate limits every minute, quotas monthly)
- Render deployment with Docker

## Architecture

### System Architecture

The system follows a layered architecture with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                     REST API Layer                          │
│  (GenerationController, QuotaController, PlanController)   │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    Service Layer                            │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │         Request Chain Orchestration                  │  │
│  │                                                      │  │
│  │  RateLimitValidator → QuotaValidator → Generator   │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                             │
│  Supporting Services:                                       │
│  - UsageTracker                                            │
│  - PlanManager                                             │
│  - LimitResetter (Scheduled)                               │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                  Repository Layer                           │
│  (UsageRepository, PlanRepository, HistoryRepository)      │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                   Data Storage                              │
│            (In-Memory / Database)                           │
└─────────────────────────────────────────────────────────────┘
```

### Request Flow

```
Client Request
     │
     ▼
[GenerationController]
     │
     ▼
[RequestOrchestrator]
     │
     ├──► [RateLimitValidator] ──► Reject (429) if exceeded
     │         │
     │         ▼ (approved)
     │    [QuotaValidator] ──► Reject (402) if insufficient
     │         │
     │         ▼ (approved)
     │    [TextGenerator]
     │         │
     │         ▼
     └──► Response (200)
```

## Components and Interfaces

### Core Service Interfaces

#### TextGenerationService

The primary interface for text generation operations.

```java
public interface TextGenerationService {
    GenerationResponse generate(GenerationRequest request);
}
```

**Responsibilities:**
- Define contract for text generation
- Support polymorphic implementations
- Maintain consistent method signature across implementations

**Implementations:**
- `SimulatedGenerator`: Mock implementation with 1200ms delay and random responses

#### RequestValidator

Interface for validation components in the request chain.

```java
public interface RequestValidator {
    ValidationResult validate(String userId, GenerationRequest request);
}
```

**Responsibilities:**
- Validate requests against specific criteria
- Return approval or rejection with reason
- Support chain composition

**Implementations:**
- `RateLimitValidator`: Enforces per-minute request limits
- `QuotaValidator`: Enforces monthly token limits

### Service Layer Components

#### RequestOrchestrator

Coordinates the request validation chain and generation process.

```java
@Service
public class RequestOrchestrator {
    private final RateLimitValidator rateLimitValidator;
    private final QuotaValidator quotaValidator;
    private final TextGenerationService textGenerator;
    
    public GenerationResponse processRequest(String userId, GenerationRequest request) {
        // Chain: Rate Limit → Quota → Generation
    }
}
```

**Responsibilities:**
- Execute validation chain in correct order
- Short-circuit on first rejection
- Delegate to text generator on approval
- Return appropriate HTTP status codes

#### RateLimitValidator

Enforces per-minute request rate limits based on subscription plan.

```java
@Service
public class RateLimitValidator implements RequestValidator {
    private final PlanRepository planRepository;
    private final UsageRepository usageRepository;
    
    public ValidationResult validate(String userId, GenerationRequest request) {
        // Check plan limits: FREE=10, PRO=60, ENTERPRISE=unlimited
    }
}
```

**Responsibilities:**
- Retrieve user's subscription plan
- Check current minute's request count
- Enforce plan-specific limits
- Return 429 with Retry-After header on rejection

**Rate Limits:**
- FREE: 10 requests/minute
- PRO: 60 requests/minute
- ENTERPRISE: Unlimited

#### QuotaValidator

Enforces monthly token quotas and deducts usage.

```java
@Service
public class QuotaValidator implements RequestValidator {
    private final PlanRepository planRepository;
    private final UsageRepository usageRepository;
    
    public ValidationResult validate(String userId, GenerationRequest request) {
        // Check monthly quota: FREE=50K, PRO=500K, ENTERPRISE=unlimited
        // Deduct tokens on approval
    }
}
```

**Responsibilities:**
- Retrieve user's subscription plan
- Check remaining monthly quota
- Deduct consumed tokens
- Return 402 Payment Required on insufficient quota

**Monthly Quotas:**
- FREE: 50,000 tokens
- PRO: 500,000 tokens
- ENTERPRISE: Unlimited

#### SimulatedGenerator

Mock implementation of text generation service.

```java
@Service
public class SimulatedGenerator implements TextGenerationService {
    public GenerationResponse generate(GenerationRequest request) {
        // Simulate 1200ms processing delay
        // Return predefined random response
    }
}
```

**Responsibilities:**
- Simulate realistic processing delay (1200ms)
- Return random predefined text responses
- Implement TextGenerationService interface

#### UsageTracker

Tracks and retrieves usage history.

```java
@Service
public class UsageTracker {
    private final HistoryRepository historyRepository;
    
    public void recordUsage(String userId, int tokensUsed);
    public List<DailyUsage> getHistory(String userId, int days);
}
```

**Responsibilities:**
- Record token consumption per request
- Retrieve daily usage for specified period
- Include zero-usage days in history

#### PlanManager

Manages subscription plan transitions.

```java
@Service
public class PlanManager {
    private final PlanRepository planRepository;
    
    public PlanDetails upgradePlan(String userId);
    public PlanDetails getCurrentPlan(String userId);
}
```

**Responsibilities:**
- Handle plan upgrades (FREE→PRO→ENTERPRISE)
- Reject upgrades at maximum tier
- Return updated plan details

#### LimitResetter

Scheduled tasks for resetting usage counters.

```java
@Service
public class LimitResetter {
    private final UsageRepository usageRepository;
    
    @Scheduled(cron = "0 * * * * *")  // Every minute
    public void resetRateLimits();
    
    @Scheduled(cron = "0 0 0 1 * *")  // First day of month at midnight
    public void resetMonthlyQuotas();
}
```

**Responsibilities:**
- Reset rate limit counters every minute
- Reset monthly quota usage on first of month
- Use Spring's @Scheduled annotation with CRON expressions

### Controller Layer

#### GenerationController

REST endpoints for text generation.

```java
@RestController
@RequestMapping("/api/ai")
public class GenerationController {
    private final RequestOrchestrator orchestrator;
    
    @PostMapping("/generate")
    public ResponseEntity<GenerationResponse> generate(
        @RequestHeader("User-Id") String userId,
        @RequestBody GenerationRequest request
    );
}
```

**Endpoints:**
- `POST /api/ai/generate`: Submit generation request

#### QuotaController

REST endpoints for quota management.

```java
@RestController
@RequestMapping("/api/quota")
public class QuotaController {
    private final UsageTracker usageTracker;
    private final PlanManager planManager;
    
    @GetMapping("/status")
    public ResponseEntity<QuotaStatus> getStatus(
        @RequestHeader("User-Id") String userId
    );
    
    @GetMapping("/history")
    public ResponseEntity<List<DailyUsage>> getHistory(
        @RequestHeader("User-Id") String userId
    );
}
```

**Endpoints:**
- `GET /api/quota/status`: Retrieve current quota status
- `GET /api/quota/history`: Retrieve 7-day usage history

#### PlanController

REST endpoints for subscription management.

```java
@RestController
@RequestMapping("/api/quota")
public class PlanController {
    private final PlanManager planManager;
    
    @PostMapping("/upgrade")
    public ResponseEntity<PlanDetails> upgradePlan(
        @RequestHeader("User-Id") String userId
    );
}
```

**Endpoints:**
- `POST /api/quota/upgrade`: Upgrade subscription plan

### Repository Layer

#### UsageRepository

Manages usage tracking data.

```java
public interface UsageRepository {
    void incrementRequestCount(String userId);
    int getRequestCount(String userId);
    void resetRequestCounts();
    
    void addTokenUsage(String userId, int tokens);
    int getMonthlyTokenUsage(String userId);
    void resetMonthlyUsage();
}
```

#### PlanRepository

Manages subscription plan data.

```java
public interface PlanRepository {
    SubscriptionPlan getPlan(String userId);
    void updatePlan(String userId, SubscriptionPlan plan);
}
```

#### HistoryRepository

Manages historical usage data.

```java
public interface HistoryRepository {
    void recordDailyUsage(String userId, LocalDate date, int tokens);
    List<DailyUsage> getDailyUsage(String userId, LocalDate startDate, LocalDate endDate);
}
```

## Data Models

### GenerationRequest

```java
public class GenerationRequest {
    private String prompt;
    private int maxTokens;
    private double temperature;
    
    // Getters, setters, validation
}
```

### GenerationResponse

```java
public class GenerationResponse {
    private String text;
    private int tokensUsed;
    private long processingTimeMs;
    
    // Getters, setters
}
```

### ValidationResult

```java
public class ValidationResult {
    private boolean approved;
    private String rejectionReason;
    private Integer retryAfterSeconds;  // For rate limiting
    
    public static ValidationResult approved() { ... }
    public static ValidationResult rejected(String reason) { ... }
    public static ValidationResult rateLimited(int retryAfter) { ... }
}
```

### SubscriptionPlan

```java
public enum SubscriptionPlan {
    FREE(10, 50_000),
    PRO(60, 500_000),
    ENTERPRISE(Integer.MAX_VALUE, Integer.MAX_VALUE);
    
    private final int requestsPerMinute;
    private final int monthlyTokens;
    
    // Constructor, getters
}
```

### QuotaStatus

```java
public class QuotaStatus {
    private int tokensUsed;
    private int tokensRemaining;
    private LocalDate resetDate;
    private SubscriptionPlan currentPlan;
    
    // Getters, setters
}
```

### DailyUsage

```java
public class DailyUsage {
    private LocalDate date;
    private int tokensUsed;
    
    // Getters, setters
}
```

### PlanDetails

```java
public class PlanDetails {
    private SubscriptionPlan plan;
    private int requestsPerMinute;
    private int monthlyTokens;
    
    // Getters, setters
}
```


## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property Reflection

After analyzing all acceptance criteria, I identified the following redundancies:

1. **Rate limit properties (3.1, 3.2, 3.3)** can be combined into a single property that tests plan-specific limits
2. **Quota enforcement properties (4.1, 4.2, 4.3)** can be combined into a single property that tests plan-specific quotas
3. **Chain invocation properties (5.1, 5.2, 5.3)** can be combined into a single property about correct chain ordering
4. **HTTP status properties (6.3, 6.4, 6.5)** can be combined into a single property about correct status codes for different outcomes
5. **Quota status properties (7.2, 7.3, 7.4, 7.5)** can be combined into a single property about complete status response
6. **Reset properties (10.2, 11.2)** can be combined into a single property about reset behavior

The consolidated properties below provide comprehensive coverage without redundancy.

### Property 1: Simulated Processing Delay

*For any* generation request, the SimulatedGenerator SHALL complete processing in approximately 1200 milliseconds (±100ms tolerance).

**Validates: Requirements 2.1**

### Property 2: Non-Empty Generation Response

*For any* generation request, the SimulatedGenerator SHALL return a non-null, non-empty text response.

**Validates: Requirements 2.2**

### Property 3: Plan-Specific Rate Limiting

*For any* subscription plan and request sequence, the RateLimitValidator SHALL enforce the correct per-minute limit: FREE plans allow exactly 10 requests, PRO plans allow exactly 60 requests, and ENTERPRISE plans allow unlimited requests.

**Validates: Requirements 3.1, 3.2, 3.3**

### Property 4: Rate Limit Rejection Response

*For any* request that exceeds the rate limit, the RateLimitValidator SHALL return a rejection with HTTP 429 status and a Retry-After header indicating seconds until reset.

**Validates: Requirements 3.4**

### Property 5: Rate Limit Approval Forwarding

*For any* request within the rate limit, the RateLimitValidator SHALL approve the request and allow it to proceed to the next validation stage.

**Validates: Requirements 3.5**

### Property 6: Plan-Specific Quota Enforcement

*For any* subscription plan and token usage pattern, the QuotaValidator SHALL enforce the correct monthly limit: FREE plans allow up to 50,000 tokens, PRO plans allow up to 500,000 tokens, and ENTERPRISE plans allow unlimited tokens.

**Validates: Requirements 4.1, 4.2, 4.3**

### Property 7: Token Deduction Accuracy

*For any* approved request with token consumption, the QuotaValidator SHALL deduct the exact number of consumed tokens from the user's remaining monthly quota.

**Validates: Requirements 4.4**

### Property 8: Insufficient Quota Rejection

*For any* request where the requested tokens exceed the remaining quota, the QuotaValidator SHALL return HTTP 402 Payment Required status.

**Validates: Requirements 4.5**

### Property 9: Sufficient Quota Forwarding

*For any* request with sufficient remaining quota, the QuotaValidator SHALL approve the request and forward it to the TextGenerationService.

**Validates: Requirements 4.6**

### Property 10: Request Chain Ordering

*For any* generation request, the RequestOrchestrator SHALL invoke validators in the correct sequence: RateLimitValidator first, then QuotaValidator, then TextGenerationService.

**Validates: Requirements 5.1, 5.2, 5.3**

### Property 11: Chain Short-Circuit on Rejection

*For any* request that is rejected at any validation stage, the RequestOrchestrator SHALL immediately return the rejection response without invoking subsequent components in the chain.

**Validates: Requirements 5.4**

### Property 12: Correct HTTP Status Codes

*For any* generation request outcome, the system SHALL return the appropriate HTTP status: 200 for successful generation, 429 for rate limit exceeded, and 402 for quota exhausted.

**Validates: Requirements 6.3, 6.4, 6.5**

### Property 13: Complete Quota Status Response

*For any* quota status request, the system SHALL return a complete response containing tokens used, tokens remaining, reset date, and current subscription plan.

**Validates: Requirements 7.2, 7.3, 7.4, 7.5**

### Property 14: Seven-Day History Completeness

*For any* usage history request, the UsageTracker SHALL return exactly 7 days of data, with each day containing a date and token count.

**Validates: Requirements 8.2, 8.3**

### Property 15: Zero-Usage Day Inclusion

*For any* day in the 7-day history period without recorded usage, the UsageTracker SHALL include that day with a token count of zero.

**Validates: Requirements 8.4**

### Property 16: Successful Upgrade Response

*For any* successful plan upgrade (FREE→PRO or PRO→ENTERPRISE), the system SHALL return HTTP 200 with the updated plan details.

**Validates: Requirements 9.5**

### Property 17: Usage Counter Reset Completeness

*For any* set of users with non-zero usage counters, the LimitResetter SHALL reset all counters to zero when the scheduled reset task executes.

**Validates: Requirements 10.2, 11.2**

## Error Handling

### Validation Errors

**Rate Limit Exceeded:**
- HTTP Status: 429 Too Many Requests
- Response Body: `{ "error": "Rate limit exceeded", "retryAfter": <seconds> }`
- Headers: `Retry-After: <seconds>`

**Quota Exhausted:**
- HTTP Status: 402 Payment Required
- Response Body: `{ "error": "Monthly quota exhausted", "tokensRemaining": 0, "resetDate": "<date>" }`

**Invalid Request:**
- HTTP Status: 400 Bad Request
- Response Body: `{ "error": "Invalid request", "details": "<validation message>" }`

**User Not Found:**
- HTTP Status: 404 Not Found
- Response Body: `{ "error": "User not found", "userId": "<userId>" }`

### Upgrade Errors

**Maximum Plan Reached:**
- HTTP Status: 400 Bad Request
- Response Body: `{ "error": "Already at maximum plan", "currentPlan": "ENTERPRISE" }`

### System Errors

**Internal Server Error:**
- HTTP Status: 500 Internal Server Error
- Response Body: `{ "error": "Internal server error", "message": "<error details>" }`
- Logging: Full stack trace logged for debugging

**Service Unavailable:**
- HTTP Status: 503 Service Unavailable
- Response Body: `{ "error": "Service temporarily unavailable" }`
- Use Case: During maintenance or system overload

### Error Handling Principles

1. **Fail Fast**: Validate requests early in the chain
2. **Clear Messages**: Provide actionable error messages
3. **Appropriate Status Codes**: Use correct HTTP status codes
4. **No Sensitive Data**: Never expose internal implementation details
5. **Logging**: Log all errors with context for debugging
6. **Graceful Degradation**: Handle partial failures gracefully

## Testing Strategy

### Testing Approach

The system requires a dual testing approach combining property-based testing for universal behaviors with example-based testing for specific scenarios and integration points.

### Property-Based Testing

**Framework:** [fast-check](https://github.com/dubzzz/fast-check) for JavaScript/TypeScript or [jqwik](https://jqwik.net/) for Java

**Configuration:**
- Minimum 100 iterations per property test
- Each test tagged with: `Feature: ai-consumption-proxy-backend, Property <N>: <property text>`
- Random seed logging for reproducibility

**Property Test Coverage:**

1. **Generator Behavior** (Properties 1-2)
   - Test with random prompts, token counts, temperature values
   - Verify timing and response validity

2. **Rate Limiting** (Properties 3-5)
   - Generate random request sequences with varying timing
   - Test all plan types (FREE, PRO, ENTERPRISE)
   - Verify limit enforcement and rejection responses

3. **Quota Management** (Properties 6-9)
   - Generate random token usage patterns
   - Test all plan types with various quota states
   - Verify deduction accuracy and rejection behavior

4. **Chain Orchestration** (Properties 10-12)
   - Generate random request scenarios (valid, rate-limited, quota-exhausted)
   - Verify correct ordering and short-circuit behavior
   - Test all possible outcome paths

5. **Status and History** (Properties 13-15)
   - Generate random usage states and history patterns
   - Verify response completeness and accuracy
   - Test sparse and dense usage patterns

6. **Plan Management** (Property 16)
   - Generate random upgrade scenarios
   - Verify response correctness

7. **Reset Operations** (Property 17)
   - Generate random user populations with various usage levels
   - Verify complete reset behavior

### Unit Testing

**Focus Areas:**
- Specific edge cases (boundary values, empty inputs)
- Error conditions and exception handling
- Individual component behavior in isolation
- Mock-based testing for dependencies

**Example Test Cases:**
- Endpoint existence and HTTP method verification (Requirements 6.1, 7.1, 8.1, 9.1)
- Plan upgrade transitions (FREE→PRO, PRO→ENTERPRISE, ENTERPRISE rejection)
- CRON expression validation (Requirements 10.3, 11.3)
- Architectural layer verification (Requirements 14.1-14.4)

### Integration Testing

**Focus Areas:**
- Controller-to-service integration
- Service-to-repository integration
- Scheduled task execution
- End-to-end request flows

**Test Scenarios:**
- Complete request flow from API endpoint to response
- Scheduled task triggering and execution
- Database persistence and retrieval
- Spring dependency injection wiring

### Deployment Testing

**Smoke Tests:**
- Dockerfile existence and validity (Requirement 12.1)
- render.yaml configuration presence (Requirement 12.4)
- Service startup and health check (Requirement 12.2)
- PORT environment variable binding (Requirement 12.3)

### Test Organization

```
src/test/java/
├── properties/          # Property-based tests
│   ├── GeneratorPropertiesTest.java
│   ├── RateLimitPropertiesTest.java
│   ├── QuotaPropertiesTest.java
│   ├── ChainPropertiesTest.java
│   └── ResetPropertiesTest.java
├── unit/               # Unit tests
│   ├── controller/
│   ├── service/
│   └── repository/
├── integration/        # Integration tests
│   ├── ApiIntegrationTest.java
│   └── SchedulingIntegrationTest.java
└── deployment/         # Deployment tests
    └── DeploymentSmokeTest.java
```

### Testing Principles

1. **Property tests verify universal behaviors** across all inputs
2. **Unit tests verify specific examples** and edge cases
3. **Integration tests verify component interactions** and wiring
4. **Smoke tests verify deployment** and configuration
5. **All tests are automated** and run in CI/CD pipeline
6. **Test coverage targets**: 80% line coverage, 100% critical path coverage


## Deployment Configuration

### Render Deployment

The application is designed for deployment on Render using Docker containerization.

#### Dockerfile

```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Build application
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port (Render will set PORT env variable)
EXPOSE ${PORT:-8080}

# Run application with PORT environment variable
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
```

**Key Features:**
- Multi-stage build for smaller image size
- Uses Eclipse Temurin JRE 17 (LTS)
- Respects Render's PORT environment variable
- Falls back to 8080 if PORT not set
- Skips tests during Docker build (run separately in CI)

#### render.yaml

```yaml
services:
  - type: web
    name: ai-consumption-proxy-backend
    env: docker
    region: oregon
    plan: starter
    branch: main
    dockerfilePath: ./Dockerfile
    envVars:
      - key: SPRING_PROFILES_ACTIVE
        value: production
      - key: JAVA_OPTS
        value: "-Xmx512m -Xms256m"
    healthCheckPath: /actuator/health
```

**Configuration Details:**
- **type**: web service (HTTP server)
- **env**: docker (uses Dockerfile)
- **region**: oregon (can be changed based on requirements)
- **plan**: starter (can be upgraded to standard/pro)
- **branch**: main (auto-deploy from main branch)
- **healthCheckPath**: Spring Boot Actuator health endpoint

#### Spring Boot Configuration

**application.yml (production profile):**

```yaml
server:
  port: ${PORT:8080}
  shutdown: graceful

spring:
  application:
    name: ai-consumption-proxy-backend
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:development}

management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: when-authorized

logging:
  level:
    root: INFO
    com.aiproxy: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"

scheduling:
  enabled: true
```

**Key Configuration:**
- **server.port**: Reads from PORT environment variable (Render requirement)
- **graceful shutdown**: Allows in-flight requests to complete
- **health endpoint**: Exposed for Render health checks
- **logging**: INFO level for production, DEBUG for application code
- **scheduling**: Enabled for rate limit and quota reset tasks

### Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| PORT | HTTP server port | 8080 | Yes (Render sets this) |
| SPRING_PROFILES_ACTIVE | Active Spring profile | development | No |
| JAVA_OPTS | JVM options | -Xmx512m -Xms256m | No |
| DATABASE_URL | Database connection URL | (in-memory) | No |

### Deployment Process

1. **Push to GitHub**: Commit code to main branch
2. **Render Auto-Deploy**: Render detects changes and triggers build
3. **Docker Build**: Render builds Docker image using Dockerfile
4. **Health Check**: Render verifies /actuator/health endpoint
5. **Traffic Routing**: Render routes traffic to new deployment
6. **Old Instance Shutdown**: Previous deployment is gracefully terminated

### Monitoring and Health Checks

**Health Check Endpoint:**
```
GET /actuator/health
```

**Response (Healthy):**
```json
{
  "status": "UP",
  "components": {
    "diskSpace": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

**Render Health Check Configuration:**
- Path: `/actuator/health`
- Interval: 30 seconds
- Timeout: 10 seconds
- Unhealthy threshold: 3 consecutive failures

### Scaling Considerations

**Horizontal Scaling:**
- Render supports horizontal scaling through plan upgrades
- Stateless design allows multiple instances
- Shared data store required for multi-instance deployment

**Vertical Scaling:**
- Adjust JAVA_OPTS for heap size based on plan
- Starter: -Xmx512m
- Standard: -Xmx1g
- Pro: -Xmx2g

**Data Persistence:**
- Current design uses in-memory storage (single instance)
- For production: migrate to PostgreSQL or Redis
- Render provides managed PostgreSQL and Redis services

### Security Considerations

1. **HTTPS**: Render provides automatic HTTPS with Let's Encrypt
2. **Environment Variables**: Sensitive data stored as Render environment variables
3. **Health Endpoint**: Limited information exposure in health checks
4. **CORS**: Configure allowed origins in production
5. **Rate Limiting**: Built-in protection against abuse

### Deployment Checklist

- [ ] Dockerfile tested locally with `docker build` and `docker run`
- [ ] render.yaml validated against Render schema
- [ ] Health endpoint returns 200 OK
- [ ] Environment variables configured in Render dashboard
- [ ] Database connection tested (if using external DB)
- [ ] Scheduled tasks verified in production
- [ ] Monitoring and alerting configured
- [ ] Backup and recovery procedures documented

## Implementation Notes

### Technology Stack

**Core Framework:**
- Spring Boot 3.x
- Java 17 (LTS)
- Maven for dependency management

**Key Dependencies:**
- spring-boot-starter-web: REST API support
- spring-boot-starter-actuator: Health checks and monitoring
- spring-boot-starter-validation: Request validation
- lombok: Reduce boilerplate code
- jqwik: Property-based testing (test scope)

**Development Tools:**
- Spring Boot DevTools: Hot reload during development
- H2 Database: In-memory database for development
- JUnit 5: Unit testing framework
- Mockito: Mocking framework

### Package Structure

```
com.aiproxy/
├── controller/
│   ├── GenerationController.java
│   ├── QuotaController.java
│   └── PlanController.java
├── service/
│   ├── generation/
│   │   ├── TextGenerationService.java
│   │   └── SimulatedGenerator.java
│   ├── validation/
│   │   ├── RequestValidator.java
│   │   ├── RateLimitValidator.java
│   │   └── QuotaValidator.java
│   ├── orchestration/
│   │   └── RequestOrchestrator.java
│   ├── tracking/
│   │   └── UsageTracker.java
│   ├── plan/
│   │   └── PlanManager.java
│   └── scheduling/
│       └── LimitResetter.java
├── repository/
│   ├── UsageRepository.java
│   ├── PlanRepository.java
│   └── HistoryRepository.java
├── model/
│   ├── request/
│   │   └── GenerationRequest.java
│   ├── response/
│   │   ├── GenerationResponse.java
│   │   ├── QuotaStatus.java
│   │   ├── DailyUsage.java
│   │   └── PlanDetails.java
│   ├── domain/
│   │   ├── SubscriptionPlan.java
│   │   └── ValidationResult.java
│   └── exception/
│       ├── RateLimitExceededException.java
│       ├── QuotaExhaustedException.java
│       └── MaxPlanReachedException.java
└── config/
    ├── SchedulingConfig.java
    └── WebConfig.java
```

### Design Patterns Applied

**Strategy Pattern:**
- TextGenerationService interface with SimulatedGenerator implementation
- Allows future real AI service implementations without changing client code

**Chain of Responsibility:**
- RequestValidator interface with RateLimitValidator and QuotaValidator
- RequestOrchestrator coordinates the validation chain
- Each validator decides to approve or reject independently

**Repository Pattern:**
- Abstraction layer for data access
- Separates business logic from persistence concerns
- Enables easy switching between in-memory and database storage

**Dependency Injection:**
- Spring-managed beans with constructor injection
- Promotes loose coupling and testability
- Facilitates mocking in unit tests

### Domain-Driven Naming Examples

**Good (Domain-Focused):**
- `RequestOrchestrator` - describes what it does (orchestrates requests)
- `UsageTracker` - tracks usage (clear business purpose)
- `PlanManager` - manages subscription plans
- `LimitResetter` - resets limits (action-oriented)

**Avoid (Pattern-Focused):**
- `AIProxyFactory` - combines pattern name with domain
- `UserQueue` - implementation detail, not business concept
- `QuotaManagerSingleton` - pattern name in business class
- `ValidationChainBuilder` - pattern-focused naming

### Configuration Management

**Development Profile (application-dev.yml):**
```yaml
spring:
  h2:
    console:
      enabled: true
logging:
  level:
    root: DEBUG
```

**Production Profile (application-prod.yml):**
```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
logging:
  level:
    root: INFO
```

### Future Enhancements

1. **Real AI Integration**: Replace SimulatedGenerator with actual AI service (OpenAI, Anthropic, etc.)
2. **Database Persistence**: Migrate from in-memory to PostgreSQL for production
3. **Caching Layer**: Add Redis for rate limit and quota tracking
4. **Authentication**: Implement JWT-based authentication
5. **API Versioning**: Support multiple API versions
6. **Metrics**: Add Prometheus metrics for monitoring
7. **Distributed Tracing**: Implement OpenTelemetry for request tracing
8. **Admin Dashboard**: Web UI for managing users and viewing analytics

### Development Workflow

1. **Local Development**: Run with `./mvnw spring-boot:run`
2. **Testing**: Execute tests with `./mvnw test`
3. **Property Tests**: Run with `./mvnw test -Dtest=*PropertiesTest`
4. **Docker Build**: Test with `docker build -t ai-proxy .`
5. **Docker Run**: Test with `docker run -p 8080:8080 -e PORT=8080 ai-proxy`
6. **Deploy**: Push to main branch for automatic Render deployment

### Performance Considerations

**Expected Load:**
- FREE users: 10 requests/minute = 14,400 requests/day
- PRO users: 60 requests/minute = 86,400 requests/day
- ENTERPRISE users: Unlimited (monitor and set reasonable limits)

**Optimization Strategies:**
- In-memory caching for plan lookups
- Efficient data structures for usage tracking
- Scheduled cleanup of old history data
- Connection pooling for database access
- Async processing for non-critical operations

**Bottlenecks:**
- Simulated 1200ms delay per generation (intentional)
- Database queries for usage tracking (optimize with indexes)
- Scheduled task execution (ensure non-blocking)

### Maintenance and Operations

**Monitoring:**
- Application logs via Render dashboard
- Health check status monitoring
- Custom metrics for rate limit hits and quota exhaustion

**Backup:**
- Database backups (if using external DB)
- Configuration backups in version control
- Environment variable documentation

**Incident Response:**
- Health check failures trigger alerts
- Rollback capability through Render dashboard
- Log analysis for debugging

**Updates:**
- Dependency updates via Dependabot
- Security patches applied promptly
- Feature releases through main branch

