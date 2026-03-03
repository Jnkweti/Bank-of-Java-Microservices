# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY proto-config/pom.xml ./proto-config/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -f proto-config/pom.xml dependency:go-offline -q

COPY proto-config/ ./proto-config/
RUN --mount=type=cache,target=/root/.m2 mvn -f proto-config/pom.xml install -DskipTests

COPY account-service/pom.xml ./account-service/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -f account-service/pom.xml dependency:go-offline -q

COPY account-service/ ./account-service/
RUN --mount=type=cache,target=/root/.m2 mvn -f account-service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre
RUN groupadd -r appuser && useradd -r -g appuser appuser
WORKDIR /app
COPY --from=builder /app/account-service/target/*.jar app.jar
USER appuser
EXPOSE 3002
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD curl -f http://localhost:3002/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]