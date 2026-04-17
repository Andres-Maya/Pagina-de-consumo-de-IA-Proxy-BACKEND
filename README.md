# AI Consumption Proxy Backend

AI Consumption Platform Backend with simulated AI text generation, rate limiting, and quota management.

## Features

- Simulated AI text generation with 1200ms processing delay
- Rate limiting based on subscription plans (FREE: 10/min, PRO: 60/min, ENTERPRISE: unlimited)
- Monthly token quotas (FREE: 50K, PRO: 500K, ENTERPRISE: unlimited)
- Usage tracking and history retrieval
- Subscription plan management and upgrades
- Automated limit resets (rate limits every minute, quotas monthly)

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Maven
- Lombok
- jqwik (property-based testing)

## Project Structure

```
src/main/java/com/aiproxy/
├── controller/     # REST API endpoints
├── service/        # Business logic
├── repository/     # Data persistence
└── model/          # Domain objects and DTOs
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Running Locally

```bash
./mvnw spring-boot:run
```

The application will start on port 8080 (or the PORT environment variable if set).

### Running Tests

```bash
./mvnw test
```

### Building

```bash
./mvnw clean package
```

## API Endpoints

- `POST /api/ai/generate` - Generate AI text
- `GET /api/quota/status` - Get current quota status
- `GET /api/quota/history` - Get 7-day usage history
- `POST /api/quota/upgrade` - Upgrade subscription plan

## Health Check

- `GET /actuator/health` - Application health status

## Configuration

The application uses the PORT environment variable for deployment compatibility (defaults to 8080).

## Deployment

See the design document for Render deployment configuration details.
