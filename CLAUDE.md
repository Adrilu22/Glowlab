# CLAUDE.md — GlowLab (Persona 1: Backend + BD)

## Instalación de skills al iniciar
Ejecuta estos comandos ANTES de cualquier tarea:

```bash
npx claude-code-templates@latest --skill development/senior-backend
npx claude-code-templates@latest --skill development/senior-frontend
npx claude-code-templates@latest --skill creative-design/ui-ux-pro-max
npx claude-code-templates@latest --skill business-marketing/seo-optimizer
```

> ⚠️ Si algún skill no instala, continúa igual — úsalos como guía de calidad:
> arquitectura limpia, separación de capas, código legible, UX consistente.

---

## ⚠️ MODO DE TRABAJO — LEE ESTO PRIMERO

**Claude Code crea la rama y escribe el código. Los commits, el push y el PR los haces TÚ.**

El flujo exacto por cada historia:
1. Dices: "trabaja en HU-XX"
2. Claude Code lee los criterios de `docs/HISTORIAS_USUARIO.md`
3. **Claude Code ejecuta `git checkout -b feature/...`** para crear y posicionarse en la rama ANTES de tocar cualquier archivo
4. Claude Code escribe o modifica únicamente los archivos de esa historia (ya dentro de la rama)
5. Al terminar, muestra el **bloque de comandos** con los `git add` y `git commit` para que TÚ los ejecutes
6. Tú haces el `git push` y abres el PR desde VS Code

**Resumen de responsabilidades:**
- Claude Code → crea la rama + escribe el código
- Tú → `git add`, `git commit`, `git push`, abrir PR

---

## Cómo abrir un Pull Request desde VS Code (sin ir al navegador)

Instala la extensión oficial: **GitHub Pull Requests** (de GitHub, Inc.)

Pasos después de hacer `git push`:
1. En VS Code, clic en el ícono de GitHub en la barra lateral izquierda (octocat)
2. Clic en **"Create Pull Request"**
3. Llena los campos:
   - **Base:** `development`
   - **Compare:** tu rama `feature/...`
   - **Title:** `[Sprint N] feat: descripción`
   - **Description:** `closes #N`
4. Clic en **"Create"**

> También: abre la paleta con `Ctrl+Shift+P` y busca `GitHub Pull Requests: Create Pull Request`

---

## Contexto del proyecto
- **Nombre:** GlowLab — Plataforma de Skincare Inteligente
- **Stack:** Spring Boot 3.2 (Java 21) + HTML/CSS/JS + PostgreSQL 15
- **Cloud:** GCP (Cloud Run + Cloud SQL, región us-central1) + Firebase Hosting (frontend)
- **Metodología:** Kanban, 3 sprints, una rama feature por historia de usuario
- **Mi rol:** Backend Developer + Product Owner (Persona 1)

---

## Estructura del repositorio
```
GlowLab/
├── CLAUDE.md
├── cloudbuild.yaml             # CI/CD con Cloud Build
├── README.md
├── .github/workflows/
│   ├── project-automation.yml  # Mueve cards del Kanban automáticamente
│   └── ci.yml                  # Deploy a Cloud Run al hacer push a main
├── backend/
│   ├── Dockerfile              # Multi-stage: Maven build + JRE Alpine
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/api_skincare/
│       │   ├── Application.java
│       │   ├── model/
│       │   │   ├── Categoria.java
│       │   │   └── Producto.java
│       │   ├── repository/
│       │   │   ├── CategoriaRepository.java
│       │   │   └── ProductoRepository.java
│       │   └── controller/
│       │       ├── CategoriaController.java
│       │       └── ProductoController.java
│       └── resources/application.properties
├── frontend/
│   ├── index.html
│   ├── style.css
│   └── script.js
├── database/
│   ├── schema.sql
│   ├── seed.sql
│   └── diagram.png
└── docs/
    ├── HISTORIAS_USUARIO.md    # ← LEER ANTES DE CADA TAREA
    ├── api-documentation.md
    └── deployment-guide.md
```

---

## Reglas de Git — OBLIGATORIAS

### Estructura de ramas
```
main          → producción final (NO tocar directamente)
development   → integración (base para crear feature/*)
feature/*     → una rama por historia de usuario (temporal)
```

### NUNCA hacer esto
- ❌ Nunca commit directo a `main` o `development`
- ❌ Nunca mezclar cambios de dos historias en una misma rama
- ❌ Nunca hacer push sin que el código compile
- ❌ Claude Code nunca ejecuta git — solo muestra los comandos

---

## Mis historias de usuario (Sprint 1, 2 y 3)

Lee los criterios completos en `docs/HISTORIAS_USUARIO.md`

| # | Historia | Rama | Issue | Sprint |
|---|---|---|---|---|
| HU-01 | Modelado de BD PostgreSQL para entidades con nuevos campos | `feature/sprint1-database-schema` | #1 | 1 |
| HU-02 | API REST - CRUD completo para Categorías | `feature/sprint1-backend-categorias` | #2 | 1 |
| HU-05 | Sistema de Login Dual y renderizado condicional por Rol | `feature/sprint2-frontend-login` | #5 | 2 |
| HU-06 | Panel de Administración Frontend para Categorías | `feature/sprint2-frontend-categorias` | #6 | 2 |
| HU-09 | Integración Fetch API: Conectar Frontend con Backend REST | `feature/sprint3-frontend-fetch-api` | #9 | 3 |
| HU-10 | Pipeline CI/CD y Despliegue en GCP/Firebase | `feature/sprint3-cicd-deploy` | #10 | 3 |

---

## Cómo procesar cada historia

Cuando el usuario diga "trabaja en HU-XX":

1. **Leer** los criterios de aceptación en `docs/HISTORIAS_USUARIO.md`
2. **Revisar** el código existente relacionado antes de modificar
3. **Crear la rama** ejecutando:
   ```bash
   git checkout development && git pull origin development
   git checkout -b feature/sprintN-nombre-historia
   ```
   Esto garantiza que todos los archivos modificados queden dentro de la rama desde el inicio.
4. **Aplicar los skills instalados** como guía de calidad al escribir el código:
   - `senior-backend` → arquitectura por capas, manejo de excepciones, DTOs si aplica
   - `senior-frontend` → JS modular, manejo de errores de API, UX fluida
   - `ui-ux-pro-max` → si hay cambios visuales, cuidar consistencia de diseño
5. **Escribir o modificar** solo los archivos de esa historia (nada más)
6. **Verificar** que cada criterio de aceptación esté cubierto en el código
7. **Mostrar el bloque de comandos** con los commits para que el usuario los ejecute

---

## Bloque de comandos — formato OBLIGATORIO al finalizar cada historia

Siempre termina la respuesta con este bloque (adaptando los valores).
La rama ya fue creada por Claude Code — el bloque empieza directo en los commits:

```
═══════════════════════════════════════════════════════════
📋 COMANDOS PARA EJECUTAR EN TU TERMINAL (VS Code)
   (La rama feature/... ya fue creada por Claude Code)
═══════════════════════════════════════════════════════════

# Commits por archivo (ejecutar en orden)
git add backend/src/main/java/com/example/api_skincare/model/Categoria.java
git commit -m "feat: agregar campos descripcion e icono a entidad Categoria JPA - closes #2"

git add backend/src/main/java/com/example/api_skincare/repository/CategoriaRepository.java
git commit -m "feat: agregar CategoriaRepository con búsqueda por nombre - closes #2"

git add backend/src/main/java/com/example/api_skincare/controller/CategoriaController.java
git commit -m "feat: implementar CRUD completo /api/categorias - closes #2"

# Subir la rama
git push origin feature/sprint1-backend-categorias

═══════════════════════════════════════════════════════════
📌 PR DESDE VS CODE (extensión GitHub Pull Requests):
   Base:    development
   Compare: feature/sprint1-backend-categorias
   Título:  [Sprint 1] feat: CRUD completo de categorías
   Body:    closes #2
═══════════════════════════════════════════════════════════
```

Los commits deben ser **granulares**: uno por archivo o grupo lógico.
Nunca un solo `git add .` para toda la historia.

---

## Stack técnico detallado

### Backend (Spring Boot)
- **Java 21**, Spring Boot 3.2
- **JPA/Hibernate** para persistencia
- **Bean Validation** (`@NotBlank`, `@Min`, `@Max`, etc.)
- **SpringDoc OpenAPI** para Swagger (NO springfox)
- **Fix HTTPS:** `server.forward-headers-strategy=framework` en application.properties
- **CORS:** `@CrossOrigin("*")` en cada controlador
- **Package base:** `com.example.api_skincare`

### Base de datos
- **PostgreSQL 15** en Cloud SQL
- Conexión via **Cloud SQL Socket Factory** (no TCP directo)
<<<<<<< HEAD
- Tablas principales: `categoria`, `producto`
=======
- Tablas principales: `categoria`, `producto`, `compra`, `detalle_compra`
>>>>>>> a09570d (docs: adaptar CLAUDE.md al proyecto GlowLab)
- Variables de entorno: `DB_USER`, `DB_PASSWORD`, `INSTANCE_CONNECTION_NAME`, `PORT`

### Endpoints que debo implementar
```
/api/categorias                    → CRUD completo
/api/categorias/{id}               → GET por id, PUT, DELETE
/api/productos                     → CRUD + ?categoriaId=
/api/productos/{id}                → GET por id con tiposPiel formateados
/api/productos/{id}/categoria      → relación categoría del producto
```

### Regla de búsqueda
- Usar `findByNombreContainingIgnoreCase()` en el repositorio
- Búsqueda parcial: buscar "Ceta" debe encontrar "Cetaphil"

---

## Checklist antes de mostrar los comandos

Claude Code verifica mentalmente antes de dar los comandos:
- [ ] El código no tiene errores de sintaxis visibles
- [ ] Todos los criterios de aceptación de la historia están cubiertos
- [ ] Los commits incluyen `closes #N` con el número correcto
- [ ] Solo se modificaron archivos de esta historia
- [ ] No hay credenciales hardcodeadas

---

## Contexto de despliegue GCP

```yaml
Proyecto GCP:    glowlab-cloud
Región:          us-central1
Servicio:        glowlab-app (Cloud Run)
Instancia SQL:   glowlab-cloud:us-central1:glowlab-db
Base de datos:   skincare
Registry:        us-central1-docker.pkg.dev/glowlab-cloud/...
Frontend:        Firebase Hosting
```

El deploy automático ocurre al hacer merge a `main` via GitHub Actions → Cloud Build.
