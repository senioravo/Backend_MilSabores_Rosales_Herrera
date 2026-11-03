# Declara la topología en el clúster y el proceso termina.
# Build:  docker build -t milsabores/rabbit-topology ./rabbit-topology

FROM gradle:8.11.1-jdk17 AS build
WORKDIR /app

COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon > /dev/null 2>&1 || true

COPY src ./src
RUN gradle bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S -g 1001 spring && adduser -S -u 1001 -G spring spring
COPY --from=build /app/build/libs/*-boot.jar app.jar
USER 1001

ENV JAVA_OPTS="-Xms64m -Xmx128m -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
