# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY proto-config/pom.xml ./proto-config/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -f proto-config/pom.xml dependency:go-offline -q

COPY proto-config/ ./proto-config/
RUN --mount=type=cache,target=/root/.m2 mvn -f proto-config/pom.xml install -DskipTests

COPY api-gateway/pom.xml ./api-gateway/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -f api-gateway/pom.xml dependency:go-offline -q

COPY api-gateway/ ./api-gateway/
RUN --mount=type=cache,target=/root/.m2 mvn -f api-gateway/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/api-gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]