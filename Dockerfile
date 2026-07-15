# ============================================
# Nova Platform — demo-notifications-quarkus
# Multi-stage Dockerfile for the Quarkus demo.
#
# Build (from this directory):
#   docker buildx build \
#     --build-context hostm2=$env:USERPROFILE\.m2\repository \
#     -t demo-notifications-quarkus:1.0.0-SNAPSHOT .
#
# The `hostm2` build context injects the locally-published Nova libraries
# (core + quarkus-extension) into the build container's Maven local repo.
# Prerequisite (one-time, and after any rebuild of the Nova core or extension):
#   cd ..\..\java\nova-java-notifications
#   .\mvnw.cmd install
#   cd ..\..\java\nova-java-notifications-quarkus-extension
#   .\gradlew.bat publishToMavenLocal
# Then build from this directory with the command shown above.
# ============================================

# syntax=docker/dockerfile:1.7

# ==== Builder ====
FROM eclipse-temurin:25-jdk-alpine AS builder

ARG NOVA_UID=1001
RUN addgroup -S -g $NOVA_UID nova \
 && adduser -S -u $NOVA_UID -G nova nova

WORKDIR /build
RUN chown -R nova:nova /build

# Copy build configuration first (Docker layer cache).
COPY --chown=nova:nova gradlew gradlew.bat settings.gradle.kts build.gradle.kts gradle.properties ./
COPY --chown=nova:nova gradle ./gradle
USER nova

# Inject Nova libraries from the host's local Maven repo.
COPY --from=hostm2 --chown=nova:nova pe/edu/nova /root/.m2/repository/pe/edu/nova

RUN --mount=type=cache,target=/home/nova/.gradle/caches,uid=1001,gid=1001 \
    ./gradlew dependencies --no-daemon || true

# Build the Quarkus fast-jar layout (build/quarkus-app/).
COPY --chown=nova:nova src ./src
RUN --mount=type=cache,target=/home/nova/.gradle/caches,uid=1001,gid=1001 \
    ./gradlew build -x test --no-daemon

# ==== Runtime ====
FROM eclipse-temurin:25-jre-alpine AS runtime

# tini: signal handling. netcat-openbsd: TCP healthcheck.
RUN apk add --no-cache tini netcat-openbsd

ARG NOVA_UID=1001
RUN addgroup -S -g $NOVA_UID nova \
 && adduser -S -u $NOVA_UID -G nova nova

# OCI + Nova traceability labels.
ARG GIT_SHA=unknown
ARG BUILD_NUMBER=local
ARG APP_CODE=DEMO-NOTIFICATIONS
ARG SERVICE_CODE=demo-notifications-quarkus
LABEL pe.edu.nova.git-sha="${GIT_SHA}" \
      pe.edu.nova.build-number="${BUILD_NUMBER}" \
      pe.edu.nova.app-code="${APP_CODE}" \
      pe.edu.nova.service-code="${SERVICE_CODE}" \
      org.opencontainers.image.source="https://github.com/ahincho/demo-notifications-quarkus" \
      org.opencontainers.image.title="demo-notifications-quarkus" \
      org.opencontainers.image.description="Nova Platform Quarkus demo: REST endpoint backed by nova-notifications-quarkus-extension." \
      org.opencontainers.image.licenses="UNLICENSED"

WORKDIR /app
RUN chown -R nova:nova /app

# Quarkus fast-jar layout: the runtime needs the WHOLE quarkus-app/ directory
# (quarkus-run.jar references the other jars in lib/main/, app/, etc.).
COPY --from=builder --chown=nova:nova /build/build/quarkus-app /app/quarkus-app

USER nova
EXPOSE 8080
STOPSIGNAL SIGTERM

# TCP-only healthcheck (the demo has no /q/health endpoint).
HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
  CMD nc -z localhost 8080 || exit 1

ENTRYPOINT ["/sbin/tini", "--", "java", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Dquarkus.http.port=8080", \
    "-jar", "/app/quarkus-app/quarkus-run.jar"]
