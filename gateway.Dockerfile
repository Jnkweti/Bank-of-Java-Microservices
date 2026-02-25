FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY proto-config/ ./proto-config/
RUN mvn -f proto-config/pom.xml install -DskipTests

COPY api-gateway/ ./api-gateway/
RUN mvn -f api-gateway/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/api-gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
