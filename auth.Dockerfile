FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY auth-service/ ./auth-service/
RUN mvn -f auth-service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/auth-service/target/*.jar app.jar
EXPOSE 3006
ENTRYPOINT ["java", "-jar", "app.jar"]
