# Multi-stage Dockerfile for Identity & RBAC Service
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy project files
COPY identity-service/pom.xml ./identity-service/
COPY identity-service/src ./identity-service/src

WORKDIR /build/identity-service
RUN mvn clean package -DskipTests

# Stage 2: Lightweight Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /build/identity-service/target/*.jar app.jar

USER appuser

EXPOSE 4001 9091

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:4001/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
