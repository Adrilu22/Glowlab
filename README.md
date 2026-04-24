# GlowLab — Plataforma de Skincare Inteligente

## Descripción del Proyecto

GlowLab es una plataforma web de skincare inteligente que permite a los usuarios explorar productos de cuidado de piel, generar rutinas personalizadas según su tipo de piel, gestionar un carrito de compras y acceder a un panel de administración. El proyecto está completamente desplegado en Google Cloud Platform y sigue una arquitectura REST con frontend estático embebido en el mismo contenedor.

**URL de producción:** https://glowlab-api-994118614969.us-central1.run.app

---

## Arquitectura General

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
```

---

## Stack Tecnológico

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
| Control de versiones | GitHub | — |

---

## Modelo de Datos

Las tablas se crean y actualizan automáticamente por Hibernate con `ddl-auto=update`. El DDL a continuación es de referencia para replicar el esquema manualmente.

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

## Sistema de Roles

| Rol | Permisos |
|---|---|
| `admin` | Accede al panel de administración: crear, editar y eliminar categorías y productos |
| `user` | Accede a la tienda, carrito de compras y generador de rutinas |

El control de acceso se implementa en el frontend con JavaScript. Después del login, el rol se almacena en `localStorage` y determina qué secciones se muestran. El backend no implementa autenticación JWT en esta versión.

## Estructura del Repositorio

```
GlowLab/
├── cloudbuild.yaml                        # Pipeline CI/CD de Cloud Build
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
│       │   └── controller/
│       │       ├── CategoriaController.java
│       │       ├── ProductoController.java
│       │       ├── CompraController.java
│       │       ├── UsuarioController.java
│       │       └── RutinaController.java
│       └── resources/
│           └── application.properties
├── frontend/
│   ├── index.html                         # SPA: tienda, login, admin, rutinas
│   ├── style.css
│   └── script.js
├── database/
│   ├── schema.sql                         # DDL de referencia
│   ├── seed.sql                           # Datos iniciales
│   └── diagram.png
└── docs/
    ├── HISTORIAS_USUARIO.md               # Criterios de aceptación por historia
    ├── api-documentation.md               # Referencia completa de la API REST
    └── deployment-guide.md
```

---

## Configuración y Ejecución Local

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

El proxy crea un socket local en el puerto 5432 para que la aplicación se conecte a Cloud SQL de forma segura. **Deja esta terminal abierta mientras desarrollas.**

```bash
./cloud-sql-proxy api-de-glowlab-api:us-central1:glowlab-db --port 5432
```

> Si no tienes el binario: `gcloud components install cloud-sql-proxy`

### Paso 4: Configurar variables de entorno

```bash
export DB_NAME=glowlab_db
export DB_USER=postgres
export DB_PASSWORD=$(gcloud secrets versions access latest --secret=db-password)
export INSTANCE_CONNECTION_NAME=glowlab-api:us-central1:glowlab-db
export PORT=8080
```

### Paso 5: Compilar y ejecutar

```bash
cd backend
mvn clean package -DskipTests
java -jar target/glowlabapi-*.jar
```

La aplicación queda disponible en:
- **Frontend:** http://localhost:8080
- **API REST:** http://localhost:8080/api/categorias

### Paso 6: Verificar que funciona

```bash
# Listar categorías
curl http://localhost:8080/api/categorias

# Listar productos
curl http://localhost:8080/api/productos

# Crear una categoría de prueba
curl -X POST http://localhost:8080/api/categorias \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Exfoliacion","descripcion":"Productos exfoliantes","icono":"✨"}'
```

---

## Pipeline CI/CD

Todo `push` o merge a la rama `main` dispara automáticamente el pipeline de Cloud Build:

```yaml
# cloudbuild.yaml
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

  # 4. Desplegar en Cloud Run con secreto de BD y conexión Cloud SQL
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

**Tiempo promedio de deploy:** 3-4 minutos.

### Dockerfile (multi-stage)

```dockerfile
# Etapa 1: compilar con Maven + JDK 21
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

# Etapa 2: imagen final ligera con JRE Alpine (~200 MB vs ~600 MB)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

> El frontend (HTML/CSS/JS) se copia dentro del JAR como recurso estático de Spring Boot bajo `src/main/resources/static/`. Un solo contenedor sirve tanto el API REST como el frontend.

---

## Sprints e Historias de Usuario

### Sprint 1 — Fundamentos de Backend y Base de Datos

| Historia | Descripción | Rama | Estado |
|---|---|---|---|
| HU-01 | Modelado de BD PostgreSQL para entidades con campos completos | `feature/sprint1-database-schema` | Completada |
| HU-02 | API REST CRUD completo para Categorías | `feature/sprint1-backend-categorias` | Completada |

**Entregables:**
- Entidades JPA: `Categoria` (nombre, descripcion, icono), `Producto` (nombre, marca, descripcion, precio, tipos_piel)
- Repositorios Spring Data JPA con búsqueda por nombre (`findByNombreContainingIgnoreCase`)
- CRUD completo para `/api/categorias` y `/api/productos`
- Esquema gestionado automáticamente por Hibernate

---

### Sprint 2 — Frontend con Roles

| Historia | Descripción | Rama | Estado |
|---|---|---|---|
| HU-05 | Sistema de Login Dual y renderizado condicional por Rol | `feature/sprint2-frontend-login` | Completada |
| HU-06 | Panel de Administración Frontend para Categorías | `feature/sprint2-frontend-categorias` | Completada |

**Entregables:**
- Login con dos roles: `admin` y `user`, persistencia en `localStorage`
- Renderizado condicional: el panel de admin solo aparece para el rol `admin`
- Panel de administración con formularios para crear/editar/eliminar categorías y productos
- Tienda con filtrado por categoría, carrito de compras y total dinámico
- Generador de rutinas con preguntas sobre tipo de piel y preocupaciones

---

### Sprint 3 — Integración y Despliegue en Producción

| Historia | Descripción | Rama | Estado |
|---|---|---|---|
| HU-09 | Integración Fetch API: conectar frontend con backend REST | `feature/sprint3-frontend-fetch-api` | Completada |
| HU-10 | Pipeline CI/CD y despliegue en GCP/Firebase | `feature/sprint3-cicd-deploy` | Completada |

**Entregables:**
- Frontend 100% conectado al API REST via `fetch()` con `async/await`
- Persistencia real de compras (`/api/compras`), usuarios (`/api/usuarios`) y rutinas (`/api/rutinas`)
- Pipeline Cloud Build automatizado: compilar → docker → push → deploy
- Servicio live en Cloud Run con Cloud SQL, Secret Manager y Artifact Registry

---

## Métricas del Proyecto

> Datos extraídos directamente del historial de git y GitHub Insights.  
> Comandos para replicar: `git log --oneline --all | wc -l` · `git log --oneline | grep "fix:" | wc -l`

### Métricas de Código

| Métrica | Valor |
|---|---|
| Endpoints REST implementados | 22 |
| Entidades JPA (tablas en PostgreSQL) | 6 |
| Controladores REST | 5 |
| Repositorios Spring Data JPA | 6 |
| Tamaño de imagen Docker final | ~200 MB |
| Tiempo promedio de deploy (Cloud Build) | 3-4 minutos |

### Métricas de Proceso (GitHub Insights)

| Métrica | Valor |
|---|---|
| Total de commits | 82 |
| Commits de nuevas funcionalidades (`feat:`) | 29 |
| Commits de corrección de errores (`fix:`) | 23 |
| Pull Requests mergeados | 12 |
| Issues cerrados | 10 (#1 al #10) |
| Sprints completados | 3 |
| Historias de usuario completadas | 10 |
| Ramas feature creadas | 8 |
| Duración total del proyecto | 3 días (20–22 abril 2026) |
| Contribuidores activos | 3 |

### Cómo consultar GitHub Insights

En el repositorio de GitHub, ir a la pestaña **Insights** y revisar:
- **Pulse** — resumen de actividad reciente (commits, PRs, issues)
- **Contributors** — commits por persona y líneas de código aportadas
- **Code frequency** — evolución del tamaño del código semana a semana
- **Network** — grafo de ramas y merges

Para generar reportes desde la terminal:

```bash
# Commits por tipo
git log --oneline | grep -c "feat:"     # funcionalidades
git log --oneline | grep -c "fix:"      # correcciones

# Commits por autor
git shortlog -s -n --all

# Actividad por fecha
git log --format="%ad" --date=short | sort | uniq -c

# Todas las ramas creadas
git branch -a | grep "feature/"
```

---

## Bugs Encontrados y Resueltos

Durante el desarrollo se identificaron y resolvieron **10 bugs** documentados en el historial de git. A continuación el registro completo:

### BUG-01 — Error fatal de conexión a base de datos
**Síntoma:** `FATAL: database "skincare" does not exist` al arrancar el servicio en Cloud Run.  
**Causa:** La variable `DB_NAME` tenía `skincare` como valor por defecto en `application.properties` y en `cloudbuild.yaml`, pero la base de datos real se llama `glowlab_db`.  
**Solución:** Corregir el valor por defecto a `glowlab_db` en ambos archivos.  
**Commits:** `211157a` · `4e2ca7a` · `389b283` · `a08b8ba`

---

### BUG-02 — Pipeline CI/CD falla al compilar (Java 21 no soportado)
**Síntoma:** Cloud Build fallaba con `Unsupported class file major version` durante `mvn package`.  
**Causa:** La imagen `gcr.io/cloud-builders/mvn` no incluye JDK 21.  
**Solución:** Reemplazar por `maven:3.9-eclipse-temurin-21` en el primer paso de `cloudbuild.yaml`.  
**Commits:** `1a3dbad` · `389b283`

---

### BUG-03 — Nombres de tablas JPA en singular no coinciden con PostgreSQL
**Síntoma:** Hibernate intentaba crear `categoria` y `producto` (singular), pero el esquema de BD usa `categorias` y `productos` (plural), generando errores de mapeo.  
**Causa:** Las entidades JPA no tenían la anotación `@Table(name=...)` explícita.  
**Solución:** Agregar `@Table(name = "categorias")` y `@Table(name = "productos")` en las entidades.  
**Commits:** `083a1fe` · `032cf11`

---

### BUG-04 — Frontend enviaba payload vacío a `/api/compras`
**Síntoma:** Las compras no se guardaban en la base de datos. El endpoint recibía `{}` o `null`.  
**Causa:** `checkoutCart()` enviaba un objeto vacío en lugar del array `[{ productoId }]` que espera el endpoint.  
**Solución:** Corregir la función para serializar el carrito como `cart.map(item => ({ productoId: item.product.id }))`.  
**Commits:** `25167a0`

---

### BUG-05 — CORS bloqueaba peticiones desde la URL de producción
**Síntoma:** Error `CORS policy: No 'Access-Control-Allow-Origin'` al acceder desde `glowlab-api-994118614969.us-central1.run.app`.  
**Causa:** La URL del servicio de Cloud Run no estaba en la lista `cors.allowed-origins`.  
**Solución:** Agregar la URL completa de Cloud Run a `cors.allowed-origins` en `application.properties`.  
**Commits:** `504cc34`

---

### BUG-06 — `API_BASE` absoluta rompía el frontend en producción
**Síntoma:** Todas las llamadas `fetch()` fallaban con error de red cuando el frontend se servía desde el mismo contenedor.  
**Causa:** `API_BASE` apuntaba a una URL absoluta externa. Al estar en el mismo origen, el navegador rechazaba la petición.  
**Solución:** Cambiar a `API_BASE = ''` (cadena vacía) para que todas las rutas sean relativas al origen actual.  
**Commits:** `99af7f8`

---

### BUG-07 — `tiposPiel` llegaba como array del backend pero el frontend esperaba string CSV
**Síntoma:** Los tipos de piel no se mostraban en las tarjetas de productos en la tienda.  
**Causa:** El backend devolvía `["seca","mixta"]` (array JSON), pero `mapProducto()` en el frontend llamaba `.split(",")` como si fuera un string.  
**Solución:** Actualizar `mapProducto()` para detectar si `tiposPiel` es un array y usarlo directamente, o aplicar `.split()` si es string.  
**Commits:** `8a205b8`

---

### BUG-08 — Permisos de ejecución de `mvnw` en CI Linux
**Síntoma:** GitHub Actions fallaba con `Permission denied: ./mvnw` en el runner de Ubuntu.  
**Causa:** El archivo `mvnw` no tenía el bit de ejecución activado en el sistema de archivos de Linux.  
**Solución:** Agregar `chmod +x mvnw` como paso previo en el workflow de GitHub Actions.  
**Commits:** `bfbce44`

---

### BUG-09 — Build context incorrecto en Docker
**Síntoma:** El paso de `docker build` fallaba porque el Dockerfile no encontraba el JAR compilado ni los recursos del frontend.  
**Causa:** El build context apuntaba al subdirectorio `backend/`, pero el Dockerfile necesita acceso a la raíz del proyecto para copiar `frontend/`.  
**Solución:** Cambiar el build context a `.` (raíz) y referenciar el Dockerfile como `-f backend/Dockerfile`.  
**Commits:** `c7b4e2e`

---

### BUG-10 — Variable `PORT` sin valor por defecto en Dockerfile
**Síntoma:** La aplicación no arrancaba al ejecutarla localmente porque `PORT` no estaba definida.  
**Causa:** `application.properties` usaba `${PORT}` sin fallback, y Cloud Run inyecta esta variable pero en local no existe.  
**Solución:** Cambiar a `server.port=${PORT:8080}` para que use 8080 cuando `PORT` no esté definida.  
**Commits:** `a0ec08e`

---

## Lecciones Aprendidas

1. **Nombre exacto de la base de datos:** La variable `DB_NAME` debe ser `glowlab_db`. Cualquier otro valor (`skincare`, `glowlab`) provoca un error fatal al inicializar el pool de conexiones Hikari en Cloud Run.

2. **Imagen Maven compatible con Java 21:** `gcr.io/cloud-builders/mvn` no incluye soporte para Java 21 y falla en el paso de compilación. La imagen correcta para Cloud Build es `maven:3.9-eclipse-temurin-21`.

3. **Cloud SQL Socket Factory vs TCP:** En Cloud Run, la conexión a Cloud SQL se establece mediante Unix socket, no TCP directo. La URL JDBC debe usar el formato `jdbc:postgresql://google/${DB_NAME}?cloudSqlInstance=PROYECTO:REGION:INSTANCIA&socketFactory=com.google.cloud.sql.postgres.SocketFactory`.

4. **Frontend embebido en el JAR:** Colocar el frontend en `src/main/resources/static/` permite que Spring Boot lo sirva automáticamente. Se simplifica enormemente el despliegue: un solo contenedor, una sola URL, sin CORS entre dominios distintos.

5. **`server.forward-headers-strategy=framework`:** Es obligatorio en Spring Boot detrás de un proxy (Cloud Run usa un load balancer interno). Sin esta configuración, los redirects generan URLs `http://` en lugar de `https://`.

6. **Compras en una sola llamada:** Enviar el array completo de productos a `POST /api/compras` (que crea la compra y todos sus detalles atómicamente) es más robusto que múltiples llamadas independientes desde el frontend, que pueden fallar parcialmente.

7. **CORS en producción:** Definir los orígenes permitidos en `application.properties` como variable (`cors.allowed-origins`) y leerlos con `@Value` en `WebConfig.java` permite configurar CORS sin recompilar el código.

---

## Links de Referencia


| Recurso | URL |
|---|---|
| Aplicación en producción | https://glowlab-api-994118614969.us-central1.run.app |
| Repositorio GitHub | https://github.com/alcarreno/Glowlab |
| Cloud Run — Consola GCP | https://console.cloud.google.com/run?project=glowlab-api |
| Artifact Registry | https://console.cloud.google.com/artifacts?project=glowlab-api|
| Cloud SQL | https://console.cloud.google.com/sql/instances/glowlab-db/overview?project=glowlab-api |
| Secret Manager | https://console.cloud.google.com/security/secret-manager?project=glowlab-api |
| Cloud Build — Historial | https://console.cloud.google.com/cloud-build/builds?project=glowlab-api |
