# Build stage
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN apk add --no-cache maven
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Render sets PORT env variable dynamically
ENV PORT=8080
EXPOSE ${PORT}

# Health check for Render monitoring
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider || exit 1

# Render uses PORT env var; ensure Spring listens on that port
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]
