# Run Chronos with Docker

This repository contains a Spring Boot application `chronos`.

This README shows two ways to run the application:
- Using Docker (recommended when you don't have Java/Maven locally or want an isolated run)
- Building and running the JAR locally (if Java + Maven are installed)


## Build & run with Docker (recommended)

From the repository root (where `Dockerfile` lives) run:

```bash
# Build a local image with an explicit tag to avoid accidental dockerhub pulls
docker build -t chronos:local .

# Run the container (foreground)
# -p maps host port 8080 -> container 8080
docker run --rm -p 8080:8080 chronos:local
```

Run detached and give it a name so you can stop it easily:

```bash
# Run in background and name the container for easy control
docker run -d --name chronos-local -p 8080:8080 chronos:local
# Tail logs
docker logs -f chronos-local
# Stop
docker stop chronos-local
# Remove (if not run with --rm)
docker rm chronos-local
```

If you want to rebuild and replace the running container:

```bash
# Remove the old container if still present
docker rm -f chronos-local || true
# Rebuild image
docker build -t chronos:local .
# Restart container
docker run -d --name chronos-local -p 8080:8080 chronos:local
```

### Common Docker problems and fixes

- Error: `pull access denied for chronos, repository does not exist or may require 'docker login'`
  - Cause: you used `docker run chronos` and Docker attempted to pull `chronos:latest` from Docker Hub.
  - Fix: build locally with an explicit tag (e.g. `chronos:local`) and run `docker run chronos:local`.

- Error: `failed to solve: ... no match for platform in manifest: not found` (e.g. when using `eclipse-temurin:17-jre-alpine`)
  - Cause: the base image tag isn't available for your host architecture (common on Apple Silicon / M1/M2).
  - Fixes:
    - Build for a specific platform (emulate amd64):
      ```bash
      docker build --platform linux/amd64 -t chronos:local .
      ```
    - Or use a different runtime base image in the `Dockerfile` (the repository already uses `eclipse-temurin:17-jre-jammy` to avoid this issue).

- Want to remove the built image:

```bash
# List images
docker images | grep chronos || true
# Remove image
docker rmi chronos:local
```


## Build & run locally (Java + Maven installed)

If you prefer to run the JAR directly on your machine, install Java 17 and Maven and then:

```bash
# Build (skip tests to speed up)
mvn -DskipTests package

# Run the produced jar
java -jar target/chronos-0.0.1-SNAPSHOT.jar
```

You can run with a different port:

```bash
java -jar target/chronos-0.0.1-SNAPSHOT.jar --server.port=8081
```


## Common runtime notes

- Default port: `8080` (change with `--server.port` or environment settings)
- Swagger / OpenAPI (if installed): try `http://localhost:8080/swagger-ui.html` or `http://localhost:8080/swagger-ui/index.html`
- Logs: when running the JAR in background, redirect logs to a file:

```bash
nohup java -jar target/chronos-0.0.1-SNAPSHOT.jar > chronos.log 2>&1 &
tail -f chronos.log
```


## Debugging tips if the app doesn't start

- Check the container logs:
  ```bash
  docker logs -f <container-name-or-id>
  ```
- If the app exits immediately, run it in the foreground to see the stack trace.
- Ensure required environment configuration (if any) is provided. The sample project uses in-memory H2 during development.


## Notes about the supplied `Dockerfile`

- Multi-stage build: first stage uses Maven + JDK 17 to build the fat JAR, second stage runs the JAR on a JRE image.
- If your host is Apple Silicon (M1/M2) and you still encounter manifest/platform errors, use the `--platform linux/amd64` build flag or change the runtime image in the Dockerfile to a multi-arch-friendly tag.


---
If you want, I can:
- Build the Docker image here and paste the build logs, or
- Run the container here (if Docker is available in this environment) and show the app logs and health checks, or
- Add a `docker-compose.yml` that also renders PlantUML image generation in a sidecar (advanced).

Which would you like next? (I can run the build here or give step-by-step debug help for errors you hit locally.)
