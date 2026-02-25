# ADR-007 – Containerise the Application with Docker

| Property    | Value                        |
|-------------|------------------------------|
| **Status**  | Accepted                     |
| **Date**    | 2025-01-01                   |
| **Authors** | Chronos Team                 |

---

## Context

Chronos must be deployable in a reproducible, environment-agnostic way across developer machines,
CI/CD pipelines, and target deployment environments (including Kubernetes – see architecture diagrams).
Developers use a mix of macOS (Intel and Apple Silicon) and Linux hosts.

## Decision

Provide a **multi-stage `Dockerfile`** that:

1. **Build stage** – uses `maven:3.9.4-eclipse-temurin-17` to compile and package the application
   (`mvn -DskipTests package`).  
   The `pom.xml` is copied before source files to maximise Docker layer caching of the Maven
   dependency download step.

2. **Runtime stage** – uses the lean `eclipse-temurin:17-jre-jammy` (Debian Jammy-based) image
   containing only the JRE, keeping the final image size small.  
   The fat JAR is copied from the build stage.

- Port `8080` is exposed.
- The entrypoint is `java -jar /app/app.jar`.
- Companion documentation (`README-DOCKER.md`) explains build and run commands, including the
  `--platform linux/amd64` flag required for Apple Silicon hosts.

## Alternatives Considered

| Alternative                       | Reason Rejected                                                         |
|-----------------------------------|-------------------------------------------------------------------------|
| Single-stage Dockerfile (with JDK) | Final image contains the full JDK and Maven cache; unnecessarily large |
| Jib (Maven plugin)                | Useful but adds a plugin dependency; Dockerfile is more universally understood |
| Spring Boot Buildpacks (`./mvnw spring-boot:build-image`) | Good option but less transparent; Dockerfile gives full control |
| `eclipse-temurin:17-jre-alpine`   | Multi-platform manifest may be absent for certain host architectures; jammy is safer |

## Consequences

**Positive**
- Identical artifact runs on any host with Docker installed, removing "works on my machine" issues.
- Multi-stage build prevents build tools (Maven, JDK) from leaking into the production image.
- Layer caching keeps rebuild times fast when only source files change.
- Enables straightforward Kubernetes deployment (see `se-deployment-k8s.mmd`).

**Negative / Trade-offs**
- Apple Silicon (M1/M2) users must pass `--platform linux/amd64` or use a compatible manifest tag.
- The fat JAR approach does not leverage Spring Boot's layered JAR optimisations by default; could be improved.
- H2 in-memory database means container state is lost on restart (by design for this simulation context).

