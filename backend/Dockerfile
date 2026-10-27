# ==============================================================================
# Derma Bliss Clinic — Multi-Stage Production Dockerfile (Spring Boot 3 / Java 21)
# ==============================================================================

# Stage 1: Build the application JAR
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace/app

# Copy Maven wrapper & POM to leverage Docker layer caching
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn

RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Copy application source code
COPY src src

# Package production executable JAR without running tests during image build
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Minimal, secure production runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add security: Run as non-privileged system user
RUN addgroup -S dermabliss && adduser -S dermabliss -G dermabliss

# Create directory for persistent uploads / logs
RUN mkdir -p /app/storage /app/logs && chown -R dermabliss:dermabliss /app

# Copy executable jar from builder
COPY --from=builder /workspace/app/target/*.jar app.jar
RUN chown dermabliss:dermabliss app.jar

USER dermabliss:dermabliss

# Configure JVM production tuning: container-aware RAM allocation & Garbage Collection
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

