FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY notification-service/ ./notification-service/
RUN mvn -f notification-service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/notification-service/target/*.jar app.jar
EXPOSE 3004
ENTRYPOINT ["java", "-jar", "app.jar"]
