# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage - using slim Debian-based image
FROM eclipse-temurin:17-jre-slim
WORKDIR /app
COPY --from=build /app/target/eventra-*.jar app.jar

# Create non-root user
RUN addgroup --system --gid 1001 appgroup && \
    adduser --system --uid 1001 --gid 1001 appuser
USER appuser

EXPOSE ${PORT:-8080}

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]