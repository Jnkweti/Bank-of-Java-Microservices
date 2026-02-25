FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY proto-config/ ./proto-config/
RUN mvn -f proto-config/pom.xml install -DskipTests

COPY customer-service/ ./customer-service/
RUN mvn -f customer-service/pom.xml package -DskipTests


FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/customer-service/target/*.jar app.jar
EXPOSE 3001
ENTRYPOINT ["java", "-jar", "app.jar"]