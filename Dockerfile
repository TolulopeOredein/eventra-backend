# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage - using Alpine for small size
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/eventra-*.jar app.jar

# Create non-root user
RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -s /bin/sh -D appuser
USER appuser

EXPOSE ${PORT:-8080}

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]