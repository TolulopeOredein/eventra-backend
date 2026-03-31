# Build stage - using Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage - using Java 21 JRE (Alpine for smaller size)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/eventra-*.jar app.jar

# Create non-root user (Alpine syntax)
RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -s /bin/sh -D appuser
USER appuser

EXPOSE ${PORT:-8080}

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]