FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY proto-config/ ./proto-config/
RUN mvn -f proto-config/pom.xml install -DskipTests

COPY account-service/ ./account-service/
RUN mvn -f account-service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/account-service/target/*.jar app.jar
EXPOSE 3002
ENTRYPOINT ["java", "-jar", "app.jar"]
