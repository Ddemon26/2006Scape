# Multi-stage build for 2006Scape Server
# Build stage
FROM maven:3.8-openjdk-8 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies (layer caching)
COPY pom.xml ./
COPY libs/ ./libs/
RUN mvn dependency:go-offline -B

# Copy source code
COPY src/ ./src/
COPY plugins/ ./plugins/

# Build the application
RUN mvn clean package -B -DskipTests

# Runtime stage
FROM openjdk:8-jre-alpine

WORKDIR /app

# Install necessary packages
RUN apk add --no-cache bash curl

# Create a non-root user
RUN addgroup -g 1000 gameserver && \
    adduser -D -s /bin/bash -u 1000 -G gameserver gameserver

# Copy the built JAR from builder stage
COPY --from=builder /app/target/server-*-jar-with-dependencies.jar ./server.jar

# Copy configuration and data files
COPY ServerConfig.json ./
COPY data/ ./data/
COPY plugins/ ./plugins/

# Create volume mount point for data persistence
VOLUME ["/app/data"]

# Change ownership of the app directory
RUN chown -R gameserver:gameserver /app

# Switch to non-root user
USER gameserver

# Expose ports (HTTP and JAGGRAB)
EXPOSE 8080 43595

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/ || exit 1

# Set environment variables
ENV JAVA_OPTS="-Xmx1g -Xms512m"

# Start the server
CMD java $JAVA_OPTS -jar server.jar