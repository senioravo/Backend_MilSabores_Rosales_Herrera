# rabbit-admin - imagen Docker (build multi-stage)
# Build:  docker build -t milsabores/rabbit-admin ./rabbit-admin

FROM gradle:8.11.1-jdk17 AS build
WORKDIR /app

COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon > /dev/null 2>&1 || true

COPY src ./src
RUN gradle bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S -g 1001 spring && adduser -S -u 1001 -G spring spring
COPY --from=build /app/build/libs/*.jar app.jar
USER 1001

ENV PORT=8086 \
    JAVA_OPTS="-XX:MaxRAMPercentage=70.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8086

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
  CMD wget -qO- "http://localhost:${PORT}/actuator/health" > /dev/null || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=$PORT -jar app.jar"]
