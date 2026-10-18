# ===================================================================
# Dockerfile para la API de Spring Boot (Multi-Stage Build)
# ===================================================================

# --- Etapa 1: Compilación de la aplicación ---
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace

# Copiar archivos del wrapper de Gradle primero para aprovechar la caché de capas de Docker
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

# Otorgar permisos de ejecución al wrapper
RUN chmod +x gradlew

# Descargar dependencias
RUN ./gradlew dependencies --no-daemon || true

# Copiar el código fuente
COPY src ./src

# Compilar el JAR ejecutable (omitiendo tests para despliegue rápido)
RUN ./gradlew bootJar -x test --no-daemon

# --- Etapa 2: Imagen final liviana de ejecución (JRE) ---
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

# Crear usuario de sistema sin privilegios de root por seguridad
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copiar el artefacto generado desde la etapa de compilación
COPY --from=build /workspace/build/libs/*.jar app.jar

# Variables de entorno por defecto (compatibles con Render, Railway, etc.)
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

# Iniciar la aplicación enlazándola al puerto dinámico provisto por el host de la nube
ENTRYPOINT ["sh", "-c", "java -Djava.security.egd=file:/dev/./urandom -Dserver.port=${PORT:-8080} -jar app.jar"]
