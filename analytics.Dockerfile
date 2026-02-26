# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY analytics-service/pom.xml ./analytics-service/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -f analytics-service/pom.xml dependency:go-offline -q

COPY analytics-service/ ./analytics-service/
RUN --mount=type=cache,target=/root/.m2 mvn -f analytics-service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/analytics-service/target/*.jar app.jar
EXPOSE 3005
ENTRYPOINT ["java", "-jar", "app.jar"]