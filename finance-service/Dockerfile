FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build

COPY finance-service/pom.xml ./
COPY finance-service/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=builder /build/target/*.jar app.jar
USER appuser
EXPOSE 4000
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:4000/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
