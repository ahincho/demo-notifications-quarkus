# demo-notifications-quarkus

Example Quarkus 3.33.2.1 LTS application consuming
[`nova-java-notifications-quarkus-extension`](../../java/nova-java-notifications-quarkus-extension).
A single REST endpoint that triggers a simulated email send through
the library's `NotificationFacade`.

## What it demonstrates

- CDI injection of the `NotificationFacade` bean (produced as a
  `@Singleton` by the extension's `NotificationsProducer`).
- A real HTTP request end-to-end through the Quarkus REST stack
  (Quarkus REST + Jackson).
- The Quarkus extension's no-op facade behavior under
  `nova.notifications.enabled=false`.

## Prerequisites

- JDK 25
- The pure library and the extension must be installed in
  `~/.m2/repository` (the demo consumes them via `mavenLocal`):

  ```bash
  cd ../java/nova-java-notifications && ./mvnw install -DskipTests
  cd ../java/nova-java-notifications-quarkus-extension && ./gradlew publishToMavenLocal
  ```

## Run (dev mode)

```bash
./gradlew quarkusDev
```

The app starts on `http://localhost:8080`. The default
`application.properties` configures the email channel with
`sendgrid` as the provider and `test-api-key-demo` as the API key
(suitable for local smoke tests).

For a production build (uber-jar):

```bash
./gradlew build
java -jar build/quarkus-app/quarkus-run.jar
```

## Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/notifications/email/welcome` | Triggers a simulated welcome email send and returns the result as JSON. |

Example:

```bash
curl http://localhost:8080/api/notifications/email/welcome
# {"sent":true,"providerMessageId":"<uuid>","channel":"email",...}
```

## Configuration

`src/main/resources/application.properties`:

```properties
nova.notifications.enabled=true
nova.notifications.email.provider=sendgrid
nova.notifications.email.api-key=test-api-key-demo
nova.notifications.email.default-sender=no-reply@example.com
nova.notifications.resilience.max-attempts=1
quarkus.http.port=8080
quarkus.http.test-port=8081
```

Override any property at runtime via env vars or Quarkus's external
config sources.

## Test

```bash
./gradlew test
```

The demo ships with one test class (`DemoApplicationTest`) that builds
a real `NotificationFacade` from a manually-constructed
`NotificationConfiguration` and exercises the
`NotificationsResource.sendWelcomeEmail` method directly. The full
HTTP integration coverage is provided by the `quarkusDev` workflow;
the resource-level unit test complements the extension module's
`QuarkusExtensionUnitTest` (which covers the producer logic in
isolation).

## Docker

The demo ships with a production-ready multi-stage `Dockerfile`
(non-root UID 1001, tini + netcat for healthchecks, OCI labels,
JVM ergonomics). Build with:

```bash
docker buildx build --build-context hostm2=$env:USERPROFILE\.m2\repository -t demo-notifications-quarkus:1.0.0-SNAPSHOT .
docker run --rm -p 8080:8080 demo-notifications-quarkus:1.0.0-SNAPSHOT
```

## Versioning

- `1.0.0-SNAPSHOT` — aligned with extension and library `1.0.0`.
- Java 25 toolchain.
- Quarkus 3.33.2.1 LTS (the last stable 3.33.x patch).

## Related

- [`nova-java-notifications`](../../java/nova-java-notifications) — pure library.
- [`nova-java-notifications-quarkus-extension`](../../java/nova-java-notifications-quarkus-extension) — Quarkus colloquial extension (this demo's dependency).
- [`examples/demo-notifications-spring-boot`](../demo-notifications-spring-boot) — same demo on Spring Boot.
- [`examples/demo-notifications-micronaut`](../demo-notifications-micronaut) — same demo on Micronaut.

---

## AI Assistance Attribution

This work was created through human-AI collaboration. The human author
(Angel Eduardo Hincho Jove, `ahincho@unsa.edu.pe`, UNSA) retains full
responsibility for the final artifact.

**AI tools used**: GitHub Copilot (Claude Opus 4.8, Sonnet 5), MiniMax
(MiniMax-M3 via paid Token Plan), OpenCode (the interactive CLI
harness used to host the session), NotebookLM, Perplexity.
Methodology: OpenSpec spec-driven development.

**Important legal note**: this artifact is **not an "AI system"** under
Article 3(1) of Regulation (EU) 2024/1689 (the EU AI Act). Article 50
transparency obligations therefore do not directly apply. This
disclosure is made voluntarily, aligned with UNESCO Principle 6
(transparency and explainability) and the R-AI requirement of the
originating challenge.

The canonical, full AI-ATTRIBUTION.md (covering the entire Nova
Platform workspace) lives at the workspace root:
[`../../AI-ATTRIBUTION.md`](../../AI-ATTRIBUTION.md).
