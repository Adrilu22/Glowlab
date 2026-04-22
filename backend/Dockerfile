# Build context: raíz del proyecto (.)
# ── Etapa 1: Compilación ──────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

# Copiar backend y frontend (index.html se embebe como recurso estático)
COPY backend/ backend/
COPY frontend/index.html backend/src/main/resources/static/index.html

# Compilar el JAR (incluye el frontend dentro)
WORKDIR /workspace/backend
RUN mvn clean package -DskipTests -q

# ── Etapa 2: Runtime (imagen ligera) ──────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/backend/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
