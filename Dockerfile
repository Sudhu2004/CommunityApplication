# -------- STAGE 1: Build using Maven --------
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy everything
COPY . .

# Build the JAR
RUN mvn clean package -DskipTests

# -------- STAGE 2: Run using lightweight JDK --------
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Run app
ENTRYPOINT ["java", "-jar", "app.jar"]
