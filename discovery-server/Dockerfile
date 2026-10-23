FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build

COPY discovery-server/pom.xml ./
COPY discovery-server/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=builder /build/target/*.jar app.jar
USER appuser
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
