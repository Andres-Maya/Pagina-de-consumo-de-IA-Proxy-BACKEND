# AI Proxy Backend

Backend service for AI consumption proxy platform.

## Deployment on Render

1. Push this repository to GitHub
2. Create a new Web Service on Render
3. Select "Build and deploy from a Git repository"
4. Connect your repository
5. Render will automatically use the `Dockerfile` and `render.yaml` blueprint

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `PORT` | Server port | 8080 |
| `DATABASE_URL` | JDBC URL | H2 in-memory |
| `DATABASE_USERNAME` | DB User | sa |
| `DATABASE_PASSWORD` | DB Password | (empty) |
| `DATABASE_DRIVER` | JDBC Driver | org.h2.Driver |

## API Endpoints

- `POST /api/ai/generate` - Generate text via AI proxy chain
- `GET /api/quota/status?userId={id}` - Check consumption status
- `GET /api/quota/history?userId={id}` - Last 7 days usage history
- `POST /api/quota/upgrade?userId={id}` - Upgrade plan (FREE -> PRO)

## Architecture

Layered architecture with Domain-Driven Design:
- **Presentation**: Controllers, DTOs, Exception Handlers
- **Application**: Use cases, Schedulers
- **Domain**: Entities, Value Objects, Repository Interfaces
- **Infrastructure**: Persistence, Proxy Chain, External Services

## Proxy Chain

Request -> RequestThrottlingGuard -> TokenConsumptionGuard -> MockTextGenerator -> Response
