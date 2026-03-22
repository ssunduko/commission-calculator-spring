# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Run
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p /app/data
VOLUME /app/data
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.main.main-class=com.chapman.edu.commissions.ai.CommissionCalculatorAiApplication"]
