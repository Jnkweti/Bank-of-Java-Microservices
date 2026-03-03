# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY proto-config/pom.xml ./proto-config/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -f proto-config/pom.xml dependency:go-offline -q

COPY proto-config/ ./proto-config/
RUN --mount=type=cache,target=/root/.m2 mvn -f proto-config/pom.xml install -DskipTests

COPY payment-service/pom.xml ./payment-service/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -f payment-service/pom.xml dependency:go-offline -q

COPY payment-service/ ./payment-service/
RUN --mount=type=cache,target=/root/.m2 mvn -f payment-service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre
RUN groupadd -r appuser && useradd -r -g appuser appuser
WORKDIR /app
COPY --from=builder /app/payment-service/target/*.jar app.jar
USER appuser
EXPOSE 3003
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD curl -f http://localhost:3003/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]