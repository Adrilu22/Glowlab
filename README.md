# GlowLab — Plataforma de Skincare

GlowLab es una plataforma web de skincare que permite explorar productos, generar rutinas personalizadas, gestionar un carrito de compras y acceder a un panel de administración. Está desplegada en Google Cloud Platform con arquitectura REST y frontend embebido en el mismo contenedor.

**URL de producción:** https://glowlab-api-994118614969.us-central1.run.app

---

## Arquitectura general

```
┌─────────────────────────────────────────────────────────────┐
│                        USUARIO FINAL                        │
│                  Chrome / Firefox / Edge                    │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTPS
┌──────────────────────────▼──────────────────────────────────┐
│                   GOOGLE CLOUD RUN                          │
│              glowlab-api (us-central1)                      │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              Spring Boot 3.2 (Java 21)               │   │
│  │  ┌──────────────┐  ┌────────────┐  ┌─────────────┐  │   │
│  │  │   /static    │  │Controllers │  │JPA/Hibernate│  │   │
│  │  │  (HTML/CSS/  │  │ REST API   │  │  Entities   │  │   │
│  │  │    JS)       │  └─────┬──────┘  └──────┬──────┘  │   │
│  │  └──────────────┘        │                │          │   │
│  └────────────────────────  │  ──────────────│──────────┘   │
│                             │ Unix Socket    │              │
│  ┌──────────────────────────▼────────────────▼───────────┐  │
│  │            Cloud SQL Auth Proxy (sidecar)             │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                           │ TCP (red privada VPC)
┌──────────────────────────▼──────────────────────────────────┐
│               CLOUD SQL (PostgreSQL 15)                     │
│         api-de-skincare:us-central1:glowlab-db              │
│                   Base de datos: glowlab_db                 │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                  CLOUD BUILD (CI/CD)                        │
│      push a main → compilar → docker build → push → deploy │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                   SECRET MANAGER                            │
│         db-password → DB_PASSWORD (env var en Cloud Run)   │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│         MONITOREO LOCAL (Proyecto 3)                        │
│   Prometheus :9090  ←  scrape /metrics  ←  API :8080       │
│   Grafana    :3000  ←  datasource       ←  Prometheus      │
└─────────────────────────────────────────────────────────────┘
```

---

## Stack tecnológico

| Capa | Tecnología | Versión |
|---|---|---|
| Backend | Spring Boot | 3.2.x |
| Lenguaje | Java | 21 |
| ORM | Hibernate / Spring Data JPA | incluido en Spring Boot |
| Base de datos | PostgreSQL | 15 |
| Frontend | HTML5 / CSS3 / JavaScript ES6+ | — |
| Contenerización | Docker (multi-stage) | 24+ |
| CI/CD | Google Cloud Build | — |
| Hosting de API | Google Cloud Run | — |
| BD gestionada | Google Cloud SQL | — |
| Secretos | Google Secret Manager | — |
| Registro de imágenes | Google Artifact Registry | — |
| Monitoreo | Prometheus + Grafana | 2.51 / 10.4 |
| IA | Groq (Llama 3.1 8B Instant) | API REST |

---

## Modelo de datos

Las tablas se crean y actualizan automáticamente por Hibernate con `ddl-auto=update`.

```sql
-- Categorías de productos de skincare
CREATE TABLE categorias (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    descripcion TEXT,
    icono       VARCHAR(50)
);

-- Productos con relación a categoría
CREATE TABLE productos (
    id           BIGSERIAL PRIMARY KEY,
    nombre       VARCHAR(200) NOT NULL,
    marca        VARCHAR(100),
    descripcion  TEXT,
    precio       DOUBLE PRECISION NOT NULL,
    tipos_piel   VARCHAR(200),        -- CSV: "seca,mixta,grasa"
    categoria_id BIGINT REFERENCES categorias(id)
);

-- Cabecera de compra
CREATE TABLE compra (
    id    BIGSERIAL PRIMARY KEY,
    fecha TIMESTAMP DEFAULT NOW()
);

-- Detalle de cada compra (un registro por producto)
CREATE TABLE detalle_compra (
    id          BIGSERIAL PRIMARY KEY,
    compra_id   BIGINT REFERENCES compra(id),
    producto_id BIGINT REFERENCES productos(id)
);

-- Usuarios registrados
CREATE TABLE usuarios (
    id            BIGSERIAL PRIMARY KEY,
    nombre        VARCHAR(150) NOT NULL,
    email         VARCHAR(200) UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    rol           VARCHAR(50) DEFAULT 'USER',   -- 'admin' | 'user'
    created_at    TIMESTAMP DEFAULT NOW()
);

-- Rutinas de cuidado personalizadas
CREATE TABLE rutinas (
    id             BIGSERIAL PRIMARY KEY,
    usuario_id     BIGINT REFERENCES usuarios(id),
    nombre         VARCHAR(200),
    tipo_piel      VARCHAR(100),
    preocupaciones TEXT,
    pasos_json     TEXT,                        -- JSON serializado como string
    created_at     TIMESTAMP DEFAULT NOW()
);
```

---

## Sistema de roles

| Rol | Permisos |
|---|---|
| `admin` | Accede al panel de administración: crear, editar y eliminar categorías y productos |
| `user` | Accede a la tienda, carrito de compras, generador de rutinas y chatbot Glow |

El control de acceso está implementado en el frontend con JavaScript. Después del login, el rol se guarda en `localStorage` y determina qué secciones se muestran. La autenticación en el backend está pendiente de implementación completa (ver sección JWT más abajo).

---

## Estructura del repositorio

```
GlowLab/
├── cloudbuild.yaml                        # Pipeline CI/CD de Cloud Build
├── docker-compose.yml                     # Stack completo: API + BD + Prometheus + Grafana
├── README.md                              # Este archivo
├── .github/
│   └── workflows/
│       ├── ci.yml                         # Smoke test de endpoints en producción
│       └── project-automation.yml        # Automatización del tablero Kanban
├── backend/
│   ├── Dockerfile                         # Multi-stage: Maven JDK 21 → JRE Alpine
│   ├── pom.xml                            # Dependencias Maven
│   └── src/main/
│       ├── java/com/example/api_skincare/
│       │   ├── Application.java
│       │   ├── config/
│       │   │   └── WebConfig.java         # Configuración CORS global
│       │   ├── model/
│       │   │   ├── Categoria.java
│       │   │   ├── Producto.java
│       │   │   ├── Compra.java
│       │   │   ├── DetalleCompra.java
│       │   │   ├── Usuario.java
│       │   │   └── Rutina.java
│       │   ├── repository/
│       │   │   ├── CategoriaRepository.java
│       │   │   ├── ProductoRepository.java
│       │   │   ├── CompraRepository.java
│       │   │   ├── DetalleCompraRepository.java
│       │   │   ├── UsuarioRepository.java
│       │   │   └── RutinaRepository.java
│       │   ├── controller/
│       │   │   ├── CategoriaController.java
│       │   │   ├── ProductoController.java
│       │   │   ├── CompraController.java
│       │   │   ├── UsuarioController.java
│       │   │   ├── RutinaController.java
│       │   │   └── ChatbotController.java  # NUEVO — Chatbot con Gemini
│       │   └── service/
│       │       └── ChatbotService.java     # NUEVO — Integración Gemini 2.0 Flash
│       └── resources/
│           ├── application.properties
│           └── application.properties.example
├── frontend/
│   ├── index.html                         # SPA: tienda, login, admin, rutinas, chatbot
│   ├── style.css
│   └── script.js
├── database/
│   ├── schema.sql                         # DDL de referencia
│   └── seed.sql                           # Datos iniciales
├── monitoring/                            # NUEVO — Stack de monitoreo
│   ├── prometheus.yml                     # Config de scraping (target: api:8080/metrics)
│   └── grafana/
│       ├── provisioning/
│       │   └── datasources/
│       │       └── prometheus.yml         # Datasource de Grafana apuntando a Prometheus
│       └── dashboards/
│           └── glowlab.json               # Dashboard con throughput, latencia y métricas
├── scripts/                               # NUEVO — Scripts de utilidad
│   ├── generate_traffic.py                # Genera tráfico de prueba (Python)
│   └── generate_traffic.ps1              # Genera tráfico de prueba (PowerShell)
└── docs/
    ├── HISTORIAS_USUARIO.md               # Criterios de aceptación por historia
    ├── api-documentation.md               # Referencia completa de la API REST
    ├── deployment-guide.md
    └── security.md                        # NUEVO — Análisis de seguridad del proyecto
```

---

## Correr el stack completo (local con Docker Compose)

Este comando levanta la API, la base de datos, Prometheus y Grafana en una sola red.

### Requisitos previos

- Docker Desktop 24+
- Una API key de Gemini (para el chatbot): https://aistudio.google.com/app/apikey

### Paso 1: Configurar la API key de Gemini

```bash
# Linux / macOS
export GEMINI_API_KEY=tu_clave_aqui

# Windows PowerShell
$env:GEMINI_API_KEY="tu_clave_aqui"
```

> Si no tienes una clave de Gemini, el chatbot muestra un mensaje de error pero el resto de la app funciona normalmente.

### Paso 2: Levantar el stack

```bash
docker-compose up --build
```

Esto inicia cuatro servicios:
- API + frontend: http://localhost:8080
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (usuario: `admin`, contraseña: `glowlab123`)

### Paso 3: Verificar que todo funciona

```bash
# API responde
curl http://localhost:8080/api/categorias

# Métricas expuestas para Prometheus
curl http://localhost:8080/metrics | head -20

# Prometheus scrapeando correctamente
# Ir a http://localhost:9090/targets → el target glowlab-api debe estar UP
```

### Paso 4: Ver el dashboard en Grafana

1. Abrir http://localhost:3000
2. Login con `admin` / `glowlab123`
3. Ir a Dashboards → "GlowLab API - Monitoreo"

El dashboard tiene paneles de throughput (requests por segundo), latencia (P50/P95/P99) y tasa de errores HTTP.

### Paso 5: Generar tráfico de prueba

Para ver las métricas en movimiento, correr el script de carga:

```bash
# Python (requiere: pip install requests)
python scripts/generate_traffic.py

# PowerShell
.\scripts\generate_traffic.ps1
```

El script hace requests continuos a los endpoints principales durante 60 segundos.

---

## Configuración y ejecución local sin Docker

Si prefieres correr solo el backend sin contenedor, necesitas Cloud SQL Auth Proxy y Java 21.

### Requisitos previos

| Herramienta | Versión mínima | Enlace |
|---|---|---|
| Java JDK | 21 | https://adoptium.net |
| Maven | 3.9 | https://maven.apache.org |
| Docker Desktop | 24+ | https://www.docker.com |
| Cloud SQL Auth Proxy | v2 | https://cloud.google.com/sql/docs/postgres/sql-proxy |
| gcloud CLI | última | https://cloud.google.com/sdk |

### Paso 1: Clonar el repositorio

```bash
git clone https://github.com/alcarreno/Glowlab.git
cd Glowlab
```

### Paso 2: Autenticar con Google Cloud

```bash
gcloud auth login
gcloud config set project glowlab-api
gcloud auth application-default login
```

### Paso 3: Levantar el proxy de Cloud SQL

```bash
./cloud-sql-proxy api-de-glowlab-api:us-central1:glowlab-db --port 5432
```

> Deja esta terminal abierta mientras desarrollas.

### Paso 4: Configurar variables de entorno

```bash
export DB_NAME=glowlab_db
export DB_USER=postgres
export DB_PASSWORD=$(gcloud secrets versions access latest --secret=db-password)
export INSTANCE_CONNECTION_NAME=glowlab-api:us-central1:glowlab-db
export PORT=8080
export GEMINI_API_KEY=tu_clave_aqui
```

### Paso 5: Compilar y ejecutar

```bash
cd backend
mvn clean package -DskipTests
java -jar target/api-skincare-*.jar
```

---

## Chatbot Glow

El chatbot está implementado como una nueva funcionalidad del Proyecto 3. Es visible en la app como un botón flotante en la esquina inferior derecha, disponible para usuarios logueados.

**Nota:** El chatbot fue desarrollado inicialmente con Google Gemini 2.0 Flash. Durante las pruebas se identificó que las claves de Gemini tienen restricciones de cuota en el tier gratuito y problemas con el formato de autenticación en cuentas nuevas. Se migró a **Groq (Llama 3.1 8B Instant)** por ser más estable, completamente gratuito sin límite diario, y con una API compatible con el estándar OpenAI.

### Cómo funciona

1. El usuario envía un mensaje desde el frontend
2. El frontend manda `POST /api/chatbot` con el mensaje y el historial de la conversación
3. El backend carga el catálogo actual de productos desde la base de datos
4. Construye un system prompt con el catálogo y llama a la API de Groq con el modelo Llama 3.1 8B
5. Devuelve la respuesta al frontend

El chatbot puede pedir el tipo de piel, preguntar por preocupaciones específicas y recomendar productos del catálogo con nombre, marca y precio. Si detecta que el usuario necesita una asesoría más personalizada, incluye un botón adicional en el chat.

### Variables de entorno requeridas

| Variable | Descripción | Cómo obtenerla |
|---|---|---|
| `GEMINI_API_KEY` | Clave para llamar a Groq (Llama 3.1) | https://console.groq.com/keys |

### Endpoint

```
POST /api/chatbot
Content-Type: application/json

{
  "mensaje": "Hola, tengo piel grasa",
  "historial": [
    { "role": "user", "content": "mensaje anterior" },
    { "role": "assistant", "content": "respuesta anterior" }
  ]
}
```

---

## Monitoreo con Prometheus y Grafana

El stack de monitoreo corre **únicamente en local** con `docker-compose up`. No está desplegado en Google Cloud Run — el enunciado del Proyecto 3 lo permite explícitamente: *"Prometheus y Grafana pueden correr localmente con Docker Compose durante la sustentación; no es obligatorio desplegarlos en la nube."*

Lo que sí está disponible en producción es el endpoint `/metrics` del API:

| Recurso | URL |
|---|---|
| Métricas en producción | https://glowlab-api-994118614969.us-central1.run.app/metrics |
| Prometheus (solo local) | http://localhost:9091 |
| Grafana (solo local) | http://localhost:3001 |

Para la sustentación: correr `docker compose up` localmente, abrir Grafana en http://localhost:3001 con `admin` / `glowlab123`, y mostrar el dashboard "GlowLab API - Monitoreo" con tráfico generado por `python scripts/generate_traffic.py`.

### Métricas expuestas en `/metrics`

La API expone métricas de Micrometer en formato Prometheus:

- `http_server_requests_seconds_count` — contador de requests por endpoint y código de respuesta
- `http_server_requests_seconds` — histograma de latencia (incluye P50, P95, P99)
- `jvm_memory_used_bytes` — uso de memoria JVM (gauge)

### Dashboard de Grafana

El archivo `monitoring/grafana/dashboards/glowlab.json` se provisiona automáticamente al levantar Grafana con Docker Compose. Contiene tres paneles:

- Throughput (requests/seg)
- Latencia P95 por endpoint
- Tasa de errores 4xx/5xx

---

## Autenticación

La autenticación está parcialmente configurada en el proyecto. `application.properties` tiene el parámetro `jwt.secret` y el docker-compose lo expone como variable de entorno `JWT_SECRET`. Sin embargo, la implementación de Spring Security (filtro JWT, controlador de login) está pendiente de agregar.

Estado actual de los endpoints:
- Los endpoints de lectura (`GET`) están abiertos (comportamiento esperado según el enunciado)
- Los endpoints de escritura (`POST`, `PUT`, `DELETE`) también están abiertos por falta de la implementación del filtro JWT

Para implementar JWT completo hace falta agregar al `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.5</version>
</dependency>
```

Y crear los archivos `SecurityConfig.java`, `JwtUtil.java`, `JwtAuthFilter.java` y `AuthController.java`.

---

## Pipeline CI/CD

Todo push o merge a `main` dispara el pipeline de Cloud Build:

```yaml
steps:
  # 1. Compilar con Maven (imagen con soporte Java 21)
  - name: 'maven:3.9-eclipse-temurin-21'
    args: ['mvn', 'clean', 'package', '-DskipTests']
    dir: 'backend'

  # 2. Construir imagen Docker (contexto = raíz del proyecto)
  - name: 'gcr.io/cloud-builders/docker'
    args:
      - build
      - -t
      - us-central1-docker.pkg.dev/glowlab-api/cloud-run-source-deploy/glowlab-api:$COMMIT_SHA
      - -f
      - backend/Dockerfile
      - .

  # 3. Subir imagen a Artifact Registry
  - name: 'gcr.io/cloud-builders/docker'
    args: ['push', '--all-tags', 'us-central1-docker.pkg.dev/...']

  # 4. Desplegar en Cloud Run
  - name: 'gcr.io/google.com/cloudsdktool/cloud-sdk'
    entrypoint: gcloud
    args:
      - run services update glowlab-api
      - --image=us-central1-docker.pkg.dev/.../skincare-api:$COMMIT_SHA
      - --region=us-central1
      - --add-cloudsql-instances=glowlab-api:us-central1:glowlab-db
      - --set-env-vars=DB_NAME=glowlab_db,DB_USER=postgres,...
      - --set-secrets=DB_PASSWORD=db-password:latest
      - --cpu=2 --memory=2Gi
```

Tiempo promedio de deploy: 3-4 minutos.

### Dockerfile (multi-stage)

```dockerfile
# Etapa 1: compilar con Maven + JDK 21
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

# Etapa 2: imagen final con JRE Alpine (~200 MB)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Sprints e historias de usuario

### Sprint 1 — Fundamentos de backend y base de datos

| Historia | Descripción | Rama | Estado |
|---|---|---|---|
| HU-01 | Modelado de BD PostgreSQL | `feature/sprint1-database-schema` | Completada |
| HU-02 | API REST CRUD completo para Categorías | `feature/sprint1-backend-categorias` | Completada |

### Sprint 2 — Frontend con roles

| Historia | Descripción | Rama | Estado |
|---|---|---|---|
| HU-05 | Sistema de Login Dual y renderizado por Rol | `feature/sprint2-frontend-login` | Completada |
| HU-06 | Panel de Administración Frontend | `feature/sprint2-frontend-categorias` | Completada |

### Sprint 3 — Integración y despliegue en producción

| Historia | Descripción | Rama | Estado |
|---|---|---|---|
| HU-09 | Integración Fetch API con backend REST | `feature/sprint3-frontend-fetch-api` | Completada |
| HU-10 | Pipeline CI/CD y despliegue en GCP | `feature/sprint3-cicd-deploy` | Completada |

### Sprint 4 — Proyecto 3: monitoreo, seguridad y chatbot

| Historia | Descripción | Rama | Estado |
|---|---|---|---|
| HU-13 | Monitoreo con Prometheus y Grafana | `feature/sprint4-monitoring` | Completada |
| HU-14 | Análisis de seguridad (security.md) | `feature/sprint4-security` | Completada |
| HU-15 | Chatbot Glow con Gemini 2.0 Flash | `feature/sprint4-chatbot` | Completada |
| HU-16 | Autenticación JWT en endpoints de escritura | `feature/sprint4-jwt-auth` | Pendiente |

---

## Métricas del proyecto

### Métricas de código

| Métrica | Valor |
|---|---|
| Endpoints REST implementados | 23 |
| Entidades JPA (tablas en PostgreSQL) | 6 |
| Controladores REST | 6 (incluye ChatbotController) |
| Repositorios Spring Data JPA | 6 |
| Servicios | 1 (ChatbotService) |
| Tamaño de imagen Docker final | ~200 MB |
| Tiempo promedio de deploy (Cloud Build) | 3-4 minutos |

### Métricas de proceso (GitHub Insights)

| Métrica | Valor |
|---|---|
| Total de commits | 82+ |
| Commits de nuevas funcionalidades (`feat:`) | 29+ |
| Commits de corrección de errores (`fix:`) | 23+ |
| Pull Requests mergeados | 12+ |
| Sprints completados | 4 |
| Historias de usuario completadas | 13+ |

---

## Bugs encontrados y resueltos

Durante el desarrollo se identificaron y resolvieron **10 bugs** documentados en el historial de git. A continuación el registro completo:

### BUG-01 — Error fatal de conexión a base de datos
**Síntoma:** `FATAL: database "skincare" does not exist` al arrancar en Cloud Run.
**Causa:** La variable `DB_NAME` tenía `skincare` como valor por defecto en `application.properties` y en `cloudbuild.yaml`, pero la base de datos real se llama `glowlab_db`.
**Solución:** Corregir el valor por defecto a `glowlab_db` en ambos archivos.
**Commits:** `211157a` · `4e2ca7a` · `389b283` · `a08b8ba`

### BUG-02 — Pipeline CI/CD falla al compilar (Java 21 no soportado)
**Síntoma:** Cloud Build fallaba con `Unsupported class file major version` durante `mvn package`.
**Causa:** La imagen `gcr.io/cloud-builders/mvn` no incluye JDK 21.
**Solución:** Reemplazar por `maven:3.9-eclipse-temurin-21` en el primer paso de `cloudbuild.yaml`.
**Commits:** `1a3dbad` · `389b283`

### BUG-03 — Nombres de tablas JPA en singular
**Síntoma:** Hibernate intentaba crear `categoria` y `producto` en singular.
**Causa:** Las entidades JPA no tenían `@Table(name=...)` explícita.
**Solución:** Agregar `@Table(name = "categorias")` y `@Table(name = "productos")`.
**Commits:** `083a1fe` · `032cf11`

### BUG-04 — Frontend enviaba payload vacío a `/api/compras`
**Síntoma:** Las compras no se guardaban. El endpoint recibía `{}` o `null`.
**Causa:** `checkoutCart()` enviaba un objeto vacío en lugar del array esperado.
**Solución:** Corregir para serializar el carrito como `cart.map(item => ({ productoId: item.product.id }))`.
**Commits:** `25167a0`

### BUG-05 — CORS bloqueaba peticiones desde la URL de producción
**Síntoma:** Error `CORS policy: No 'Access-Control-Allow-Origin'`.
**Causa:** La URL de Cloud Run no estaba en `cors.allowed-origins`.
**Solución:** Agregar la URL completa de Cloud Run a `application.properties`.
**Commits:** `504cc34`

### BUG-06 — `API_BASE` absoluta rompía el frontend en producción
**Síntoma:** Todas las llamadas `fetch()` fallaban con error de red.
**Causa:** `API_BASE` apuntaba a una URL absoluta externa.
**Solución:** Cambiar a `API_BASE = ''` para que todas las rutas sean relativas al origen.
**Commits:** `99af7f8`

### BUG-07 — `tiposPiel` llegaba como array del backend pero el frontend esperaba string CSV
**Síntoma:** Los tipos de piel no se mostraban en las tarjetas de productos.
**Causa:** El backend devolvía `["seca","mixta"]` pero el frontend llamaba `.split(",")`.
**Solución:** Actualizar `mapProducto()` para detectar si `tiposPiel` es array o string.
**Commits:** `8a205b8`

### BUG-08 — Permisos de ejecución de `mvnw` en CI Linux
**Síntoma:** GitHub Actions fallaba con `Permission denied: ./mvnw`.
**Causa:** El archivo `mvnw` no tenía el bit de ejecución en Linux.
**Solución:** Agregar `chmod +x mvnw` como paso previo.
**Commits:** `bfbce44`

### BUG-09 — Build context incorrecto en Docker
**Síntoma:** El paso de `docker build` no encontraba el JAR compilado.
**Causa:** El build context apuntaba al subdirectorio `backend/`.
**Solución:** Cambiar el build context a `.` (raíz) y referenciar el Dockerfile con `-f backend/Dockerfile`.
**Commits:** `c7b4e2e`

### BUG-10 — Variable `PORT` sin valor por defecto en Dockerfile
**Síntoma:** La aplicación no arrancaba localmente porque `PORT` no estaba definida.
**Causa:** `application.properties` usaba `${PORT}` sin fallback.
**Solución:** Cambiar a `server.port=${PORT:8080}`.
**Commits:** `a0ec08e`

---

## Lecciones aprendidas

**Nombre exacto de la base de datos.** La variable `DB_NAME` debe ser `glowlab_db`. Cualquier otro valor provoca un error fatal al inicializar el pool de conexiones Hikari en Cloud Run.

**Imagen Maven compatible con Java 21.** `gcr.io/cloud-builders/mvn` no incluye soporte para Java 21. La imagen correcta para Cloud Build es `maven:3.9-eclipse-temurin-21`.

**Cloud SQL Socket Factory vs TCP.** En Cloud Run, la conexión a Cloud SQL usa Unix socket. La URL JDBC debe usar el formato `jdbc:postgresql://google/${DB_NAME}?cloudSqlInstance=PROYECTO:REGION:INSTANCIA&socketFactory=com.google.cloud.sql.postgres.SocketFactory`.

**Frontend embebido en el JAR.** Colocar el frontend en `src/main/resources/static/` hace que Spring Boot lo sirva automáticamente. Un solo contenedor, una sola URL, sin CORS entre dominios distintos.

**`server.forward-headers-strategy=framework`.** Obligatorio en Spring Boot detrás de un proxy. Sin esto, los redirects generan URLs `http://` en lugar de `https://`.

**Variables de entorno para secretos externos.** La API key de Gemini y el JWT secret nunca deben quedar en el código fuente. Siempre se pasan como variables de entorno (`GEMINI_API_KEY`, `JWT_SECRET`) y en producción se gestionan via Secret Manager.

---

## Links de referencia

| Recurso | URL |
|---|---|
| Aplicación en producción | https://glowlab-api-994118614969.us-central1.run.app |
| Repositorio GitHub | https://github.com/alcarreno/Glowlab |
| Cloud Run — Consola GCP | https://console.cloud.google.com/run?project=glowlab-api |
| Artifact Registry | https://console.cloud.google.com/artifacts?project=glowlab-api |
| Cloud SQL | https://console.cloud.google.com/sql/instances/glowlab-db/overview?project=glowlab-api |
| Secret Manager | https://console.cloud.google.com/security/secret-manager?project=glowlab-api |
| Cloud Build — Historial | https://console.cloud.google.com/cloud-build/builds?project=glowlab-api |
| Groq Console (API Keys) | https://console.groq.com/keys |
