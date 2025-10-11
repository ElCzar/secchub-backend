# ==============================================================================
# SECCHUB BACKEND - MULTI-STAGE DOCKERFILE
# ==============================================================================
# This Dockerfile creates an optimized Docker image for the SecHub Backend
# Spring Boot application using multi-stage builds for smaller image size.
# ==============================================================================

# ==============================================================================
# STAGE 1: Build Stage
# ==============================================================================
FROM maven:3.9.11-eclipse-temurin-21-noble AS builder

# Set working directory
WORKDIR /app

# Copy Maven configuration files
COPY pom.xml ./
COPY .mvn .mvn

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -B

# ==============================================================================
# STAGE 2: Runtime Stage
# ==============================================================================
FROM eclipse-temurin:21.0.8_9-jre-noble AS runtime

# Install curl for health checks and other utilities
# Create a non-root user for security
RUN apk add --no-cache curl && \
    addgroup -g 1001 -S secchub && \
    adduser -S secchub -u 1001 -G secchub

# Set working directory
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Create logs directory and set permissions
RUN mkdir -p /app/logs && \
    chown -R secchub:secchub /app

# Switch to non-root user
USER secchub

# Expose the application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Entry point
ENTRYPOINT ["sh", "-c", "java -jar app.jar"]

# ==============================================================================
# BUILD INSTRUCTIONS
# ==============================================================================
# To build the image:
#   docker build -t secchub-backend:latest .
#
# To run the container:
#   docker run -p 8080:8080 --name secchub-backend secchub-backend:latest
#
# To run with custom environment variables (recommmended for deployment):
#   docker run -p 8080:8080 \
#     -e DB_URL=jdbc:mysql://mysql:3306/secchub \
#     -e DB_USERNAME=user \
#     -e DB_PASSWORD=password \
#     -e JWT_SECRET=your-secret-key \
#     --name secchub-backend secchub-backend:latest
# ==============================================================================