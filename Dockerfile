# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Cache Maven dependencies before copying source (layer-cache optimisation).
# The Quarkus plugin may fail offline, so errors are suppressed gracefully.
COPY pom.xml .
RUN mvn -B dependency:go-offline -q 2>/dev/null || true

COPY src ./src
RUN mvn -B package -DskipTests -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-jammy

# GTK 3 + OpenGL + X11 extensions required by OpenJFX on Linux
RUN apt-get update && apt-get install -y --no-install-recommends \
        libgtk-3-0 \
        libgl1 \
        libxrender1 \
        libxtst6 \
        libxxf86vm1 \
        curl \
        unzip \
    && rm -rf /var/lib/apt/lists/*

# Download the OpenJFX SDK for Linux/x64 (Gluon build, matches pom.xml version)
ARG JAVAFX_VERSION=21.0.2
RUN curl -fsSL \
        "https://download2.gluonhq.com/openjfx/${JAVAFX_VERSION}/openjfx-${JAVAFX_VERSION}_linux-x64_bin-sdk.zip" \
        -o /tmp/javafx.zip \
    && unzip -q /tmp/javafx.zip -d /opt/ \
    && mv "/opt/javafx-sdk-${JAVAFX_VERSION}" /opt/javafx-sdk \
    && rm /tmp/javafx.zip

WORKDIR /app

# Copy the Quarkus fast-jar output (quarkus-run.jar + lib/ + app/ + quarkus/)
COPY --from=builder /app/target/quarkus-app ./quarkus-app

# Optional: mount client_secret.json here at runtime for Google Calendar OAuth2
# docker run -v ./client_secret.json:/app/client_secret.json ...

# X display — override with -e DISPLAY=:0 or via docker-compose
ENV DISPLAY=:0

ENTRYPOINT ["java", \
    "--module-path", "/opt/javafx-sdk/lib", \
    "--add-modules", "javafx.controls,javafx.fxml", \
    "-jar", "quarkus-app/quarkus-run.jar"]
