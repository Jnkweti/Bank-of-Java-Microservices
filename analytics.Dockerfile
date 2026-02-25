FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY analytics-service/ ./analytics-service/
RUN mvn -f analytics-service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/analytics-service/target/*.jar app.jar
EXPOSE 3005
ENTRYPOINT ["java", "-jar", "app.jar"]
