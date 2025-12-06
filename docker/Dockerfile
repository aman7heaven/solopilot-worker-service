# ---------- Builder stage ----------
FROM maven:3.9.5-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy Maven modules
COPY common-framework/pom.xml common-framework/
COPY worker-service/pom.xml worker-service/

# Copy full source code
COPY common-framework/ common-framework/
COPY worker-service/ worker-service/

# Build both modules
RUN mvn clean install -f common-framework/pom.xml -DskipTests && \
    mvn clean package -f worker-service/pom.xml -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-jammy AS runtime

WORKDIR /app

# Switch to the root (admin) user inside the container.
# Only the root user is allowed to install system packages.
USER root

# Update the package index (list of available packages),
# install 'curl' without unnecessary extra packages,
# then clean up the package cache to keep the image small.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Switch back to a non-root user for better security.
# Your app should NOT run as root inside production containers.
USER 1000

# Copy final fat JAR from builder
COPY --from=builder /build/worker-service/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
