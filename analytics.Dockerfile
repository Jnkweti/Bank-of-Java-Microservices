# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY analytics-service/pom.xml ./analytics-service/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -f analytics-service/pom.xml dependency:go-offline -q

COPY analytics-service/ ./analytics-service/
RUN --mount=type=cache,target=/root/.m2 mvn -f analytics-service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre
RUN groupadd -r appuser && useradd -r -g appuser appuser
WORKDIR /app
COPY --from=builder /app/analytics-service/target/*.jar app.jar
USER appuser
EXPOSE 3005
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD curl -f http://localhost:3005/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]