# Multi-stage build for job service
FROM eclipse-temurin:21-jdk-alpine AS build

# Set working directory
WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY settings.gradle .
COPY build.gradle .

# Copy service source code
COPY service-job service-job

# Build the application
RUN chmod +x gradlew
RUN ./gradlew :service-job:build -x test

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Add non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/service-job/build/libs/*.jar app.jar

# Change ownership to appuser
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/api/v1/jobs/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]