# Multi-stage build for application service
FROM eclipse-temurin:21-jdk AS build

# Set working directory
WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY settings.gradle .
COPY build.gradle .

RUN gradle dependencies || return 0

# Copy service source code
COPY service-application service-application

# Build the application
RUN rm -rf /root/.gradle/caches/
RUN chmod +x gradlew
RUN chmod +x /app/service-application/protoc-gen-grpc-java-1.73.0-linux-x86_64.exe
RUN ./gradlew :service-application:build -x test

# Runtime stage
FROM eclipse-temurin:21-jre

# Add non-root user for security
RUN addgroup --gid 1001 --system appgroup \
    && adduser --uid 1001 --system --ingroup appgroup appuser


# Set working directory
WORKDIR /app

# Create directory for resume uploads
RUN mkdir -p /app/uploads/resumes && \
    chown -R appuser:appgroup /app/uploads

# Copy the built JAR from build stage
COPY --from=build /app/service-application/build/libs/*.jar app.jar

# Change ownership to appuser
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/api/v1/applications/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]