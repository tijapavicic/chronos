# Multi-stage Dockerfile for Chronos Spring Boot application
# Build stage: use Maven with JDK 17 to build the fat jar
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
# copy only the minimal sources first to leverage docker cache
COPY src ./src
RUN mvn -DskipTests package -DskipITs -e -B

# Run stage: use a Temurin runtime image that has multi-platform manifests
# Note: the alpine jre tag may not be available for all platforms; use a debian/jammy-based JRE tag instead.
FROM eclipse-temurin:17-jre-jammy
ARG JAR_FILE=target/chronos-0.0.1-SNAPSHOT.jar
WORKDIR /app
COPY --from=build /workspace/${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

# If you still get platform/manifest errors on certain hosts (M1/M2 Macs), you can:
#  - pass a platform to docker build: docker build --platform linux/amd64 -t chronos:local .
#  - or switch the runtime stage to an image that supports your platform (e.g. openjdk:17-jdk-slim)
