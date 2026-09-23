# ============================================================
#  usuario-service - imagen Docker (build multi-stage)
#  Build:  docker build -t milsabores/usuario-service ./usuario-service
#  Run:    docker run -p 8081:8081 --env-file .env milsabores/usuario-service
# ============================================================

# ---------- Etapa 1: compilación con Gradle ----------
FROM gradle:8.11.1-jdk17 AS build
WORKDIR /app

# Primero solo los scripts de build, para cachear las dependencias
# en una capa propia (se reutiliza mientras build.gradle no cambie)
COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon > /dev/null 2>&1 || true

COPY src ./src
RUN gradle bootJar --no-daemon -x test

# ---------- Etapa 2: runtime liviano (solo JRE) ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Usuario sin privilegios con UID numérico (lo exige runAsNonRoot en Kubernetes)
RUN addgroup -S -g 1001 spring && adduser -S -u 1001 -G spring spring
COPY --from=build /app/build/libs/*.jar app.jar
USER 1001

# PORT: Render lo inyecta automáticamente; en Docker/K8s se usa el valor por defecto.
# JAVA_OPTS: ajusta la heap al límite de memoria del contenedor.
ENV PORT=8081 \
    JAVA_OPTS="-XX:MaxRAMPercentage=70.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
  CMD wget -qO- "http://localhost:${PORT}/actuator/health" > /dev/null || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=$PORT -jar app.jar"]
