# Stage 1: Build
# -Pverticalslice tells the spring-boot-maven-plugin to bake the
# verticalslice main class into the jar manifest. Without it the default
# profile packages the ORM app, and the resulting container boots the
# wrong module.
#
# JDK 23 (instead of 21) because spring-cloud-contract-maven-plugin 4.1.4
# declares a Java 22 prerequisite — Maven enforces that check before the
# plugin's own <skip>true</skip> takes effect. The resulting jar still
# targets Java 21 bytecode and runs fine on the Temurin 21 JRE below.
FROM maven:3.9-eclipse-temurin-23 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B -Pverticalslice
COPY src ./src
RUN mvn clean package -DskipTests -B -Pverticalslice

# Stage 2: Run
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p /app/data
VOLUME /app/data
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
