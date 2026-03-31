# Build stage
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/eventra-*.jar app.jar

# Create non-root user
RUN addgroup --system --gid 1001 appgroup && \
    adduser --system --uid 1001 --gid 1001 appuser
USER appuser

# Use Render's PORT environment variable
EXPOSE ${PORT:-8080}

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]