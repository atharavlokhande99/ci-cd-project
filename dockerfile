# Multi-Stage Docker Build for Java 17/21 Maven Application
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app
COPY app/pom.xml ./
COPY app/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
