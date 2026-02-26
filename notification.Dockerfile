# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY notification-service/pom.xml ./notification-service/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -f notification-service/pom.xml dependency:go-offline -q

COPY notification-service/ ./notification-service/
RUN --mount=type=cache,target=/root/.m2 mvn -f notification-service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/notification-service/target/*.jar app.jar
EXPOSE 3004
ENTRYPOINT ["java", "-jar", "app.jar"]