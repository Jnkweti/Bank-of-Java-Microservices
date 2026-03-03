# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# proto-config deps — cached as long as its pom.xml doesn't change
COPY proto-config/pom.xml ./proto-config/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -f proto-config/pom.xml dependency:go-offline -q

# build proto-config (source rarely changes)
COPY proto-config/ ./proto-config/
RUN --mount=type=cache,target=/root/.m2 mvn -f proto-config/pom.xml install -DskipTests

# service deps — cached as long as pom.xml doesn't change
COPY customer-service/pom.xml ./customer-service/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -f customer-service/pom.xml dependency:go-offline -q

# build service (only this layer reruns on source changes)
COPY customer-service/ ./customer-service/
RUN --mount=type=cache,target=/root/.m2 mvn -f customer-service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre
RUN groupadd -r appuser && useradd -r -g appuser appuser
WORKDIR /app
COPY --from=builder /app/customer-service/target/*.jar app.jar
USER appuser
EXPOSE 3001
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD curl -f http://localhost:3001/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
