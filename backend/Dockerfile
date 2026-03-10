# Stage 1: Build the application using Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy dependency descriptor first to cache downloaded dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source and build the JAR (skip tests as they run separately)
COPY src ./src
RUN mvn package -DskipTests

# Stage 2: Run with a lightweight JRE-only Alpine image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 4000
ENTRYPOINT ["java", "-jar", "app.jar"]
