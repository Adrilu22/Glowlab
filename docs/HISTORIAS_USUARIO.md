# 📖 Historias de Usuario — GlowLab
# Formato requerido por el enunciado del Proyecto 2
# Mínimo 2 historias por persona por sprint = 6 historias por persona

# ══════════════════════════════════════════════════════════════
# SPRINT 1 — Backend, Base de Datos y Seguridad
# ══════════════════════════════════════════════════════════════

---

## HU-01 | Modelado de base de datos PostgreSQL para entidades con nuevos campos
**Rama:** `feature/sprint1-database-schema`
**Responsable:** @Adriana Carreño
**Sprint:** 1 | **Estimación:** 3 puntos | **Tipo:** Feature | **Prioridad:** Alta

### Como administradora de base de datos
Quiero actualizar el esquema de la base de datos con los nuevos campos requeridos por el frontend
Para soportar íconos en categorías, y descripciones, marcas y tipos de piel en los productos

### Criterios de Aceptación:
- [ ] El archivo `schema.sql` crea la tabla `categoria` incluyendo las columnas `descripcion` e `icono`
- [ ] El archivo `schema.sql` crea la tabla `producto` incluyendo `marca`, `descripcion` y `tipos_piel`
- [ ] El archivo `seed.sql` inserta datos iniciales que coinciden con el mock del frontend (ej: ícono 🫧 para Limpiadores)
- [ ] Las clases Entity (Producto, Categoria) en Spring Boot reflejan exactamente estas nuevas columnas

---

## HU-02 | API REST - CRUD completo para Categorías
**Rama:** `feature/sprint1-backend-categorias`
**Responsable:** @Adriana Carreño
**Sprint:** 1 | **Estimación:** 3 puntos | **Tipo:** Feature | **Prioridad:** Alta

### Como desarrolladora backend
Quiero construir un controlador REST específico para las categorías
Para que el frontend pueda listar, crear, actualizar y eliminar las categorías del catálogo

### Criterios de Aceptación:
- [ ] `GET /api/categorias` retorna la lista de categorías en JSON
- [ ] `GET /api/categorias/{id}` retorna una categoría específica o código 404 si no existe
- [ ] `POST /api/categorias` permite registrar una nueva categoría
- [ ] `PUT /api/categorias/{id}` actualiza los datos (nombre, descripción, ícono) de una categoría existente
- [ ] `DELETE /api/categorias/{id}` elimina la categoría correctamente

---

## HU-03 | API REST - CRUD completo para Productos y Filtros
**Rama:** `feature/sprint1-backend-productos`
**Responsable:** @Sary Ariza Vargas
**Sprint:** 1 | **Estimación:** 5 puntos | **Tipo:** Feature | **Prioridad:** Alta

### Como desarrolladora backend
Quiero construir un controlador REST para el inventario de productos
Para que el panel de administración pueda gestionar el catálogo y los usuarios puedan ver recomendaciones

### Criterios de Aceptación:
- [ ] `GET /api/productos` retorna todos los productos e incluye soporte para filtrar por `categoriaId`
- [ ] `GET /api/productos/{id}` retorna el producto con sus arrays de `tiposPiel` formateados
- [ ] `POST /api/productos` y `PUT /api/productos/{id}` validan que la categoría asociada exista antes de guardar
- [ ] `DELETE /api/productos/{id}` elimina el registro y devuelve un código 204 No Content

---

## HU-04 | Configuración de CORS y Variables de Entorno en Spring Boot
**Rama:** `feature/sprint1-devops-cors-env`
**Responsable:** @Sary Ariza Vargas
**Sprint:** 1 | **Estimación:** 3 puntos | **Tipo:** DevOps | **Prioridad:** Alta

### Como ingeniera DevOps
Quiero configurar los accesos de red y proteger las credenciales
Para que la API acepte peticiones del frontend de forma segura y se conecte a Cloud SQL sin exponer contraseñas

### Criterios de Aceptación:
- [ ] La configuración CORS de Spring Boot permite peticiones desde `localhost:3000` y el dominio de Firebase
- [ ] El archivo `application.properties` utiliza variables de entorno (ej: `${DB_PASS}`) en lugar de contraseñas planas
- [ ] Se crea un archivo `application.properties.example` para guiar a otros desarrolladores
- [ ] El `Dockerfile` lee la variable `$PORT` asignada dinámicamente por Cloud Run

# ══════════════════════════════════════════════════════════════
# SPRINT 2 — Integración y UI (Frontend)
# ══════════════════════════════════════════════════════════════

---

## HU-05 | Sistema de Login Dual y renderizado condicional por Rol
**Rama:** `feature/sprint2-frontend-login`
**Responsable:** @Adriana Carreño
**Sprint:** 2 | **Estimación:** 3 puntos | **Tipo:** Feature | **Prioridad:** Alta

### Como usuaria del sistema
Quiero iniciar sesión de forma segura
Para acceder a la plataforma viendo únicamente las funciones que corresponden a mi rol (Admin o Usuario)

### Criterios de Aceptación:
- [ ] La pantalla de autenticación valida correctamente las credenciales (admin123 / user123)
- [ ] El menú de navegación oculta la pestaña "Categorías" si el usuario logueado no es administrador
- [ ] El botón "Agregar producto" se desactiva para usuarios con rol estándar
- [ ] El Navbar muestra de forma dinámica las iniciales y el rol del usuario autenticado

---

## HU-06 | Panel de Administración Frontend para Categorías
**Rama:** `feature/sprint2-frontend-categorias`
**Responsable:** @Adriana Carreño
**Sprint:** 2 | **Estimación:** 3 puntos | **Tipo:** Feature | **Prioridad:** Alta

### Como administradora
Quiero una interfaz visual para gestionar las categorías
Para no depender de herramientas de base de datos al momento de actualizar el catálogo

### Criterios de Aceptación:
- [ ] La vista de Categorías carga correctamente el grid de tarjetas con sus respectivos íconos emojis
- [ ] El botón "Agregar categoría" abre el modal correspondiente con los campos limpios
- [ ] El botón "Editar" en una tarjeta pre-llena el modal con la información actual de esa categoría
- [ ] Se implementa un modal de confirmación antes de ejecutar la función de eliminar

---

## HU-07 | Panel de Administración Frontend para Productos
**Rama:** `feature/sprint2-frontend-productos`
**Responsable:** @Sary Ariza Vargas
**Sprint:** 2 | **Estimación:** 5 puntos | **Tipo:** Feature | **Prioridad:** Alta

### Como administradora
Quiero un formulario visual para el registro y edición de inventario
Para mantener los productos de skincare actualizados con sus precios y tipos de piel compatibles

### Criterios de Aceptación:
- [ ] La sección de Productos renderiza correctamente los "tags" de tipo de piel (grasa, mixta, etc.)
- [ ] El modal de "Nuevo producto" carga dinámicamente el `<select>` con las categorías existentes
- [ ] La función de editar producto guarda los cambios reflejándolos inmediatamente en el dashboard
- [ ] La barra de búsqueda filtra productos en tiempo real por nombre o marca

---

## HU-08 | Módulo Inteligente de Generación de Rutinas
**Rama:** `feature/sprint2-frontend-rutinas`
**Responsable:** @Sary Ariza Vargas
**Sprint:** 2 | **Estimación:** 5 puntos | **Tipo:** Feature | **Prioridad:** Media

### Como usuaria
Quiero ingresar mi tipo de piel y preocupaciones
Para que GlowLab me genere automáticamente una rutina de skincare ordenada por pasos

### Criterios de Aceptación:
- [ ] El Wizard de selección permite marcar un único tipo de piel y múltiples preocupaciones
- [ ] Al hacer clic en "Generar mi rutina", el algoritmo busca productos cuyo atributo `tiposPiel` coincida con la selección
- [ ] La rutina se estructura visualmente en pasos (Limpieza, Tónico, Sérum, etc.)
- [ ] El usuario puede ver el historial de sus rutinas generadas en la parte inferior de la sección

# ══════════════════════════════════════════════════════════════
# SPRINT 3 — Automatización, Conexión y Despliegue
# ══════════════════════════════════════════════════════════════

---

## HU-09 | Integración Fetch API: Conectar Frontend estático con Backend REST
**Rama:** `feature/sprint3-frontend-fetch-api`
**Responsable:** @Adriana Carreño
**Sprint:** 3 | **Estimación:** 5 puntos | **Tipo:** Feature | **Prioridad:** Alta

### Como usuaria
Quiero que los datos mostrados en la interfaz sean reales
Para que mi experiencia en la plataforma refleje el inventario real almacenado en la nube

### Criterios de Aceptación:
- [ ] Las funciones `fetchProductsFromAPI()` y `fetchCategoriesFromAPI()` reemplazan los arrays estáticos del archivo JS
- [ ] El frontend maneja peticiones asíncronas con `async/await`
- [ ] Las acciones del Admin (Crear, Editar, Eliminar) envían el payload correspondiente usando el método HTTP adecuado (POST/PUT/DELETE)
- [ ] Si la API de Cloud Run no responde, se captura el error (`catch`) sin romper la interfaz gráfica

---

## HU-10 | Pipeline CI/CD y Despliegue en GCP/Firebase
**Rama:** `feature/sprint3-cicd-deploy`
**Responsable:** @Adriana Carreño
**Sprint:** 3 | **Estimación:** 5 puntos | **Tipo:** DevOps | **Prioridad:** Alta

### Como administradora del proyecto
Quiero automatizar los despliegues a la nube
Para tener entornos de producción actualizados de manera confiable y continua

### Criterios de Aceptación:
- [ ] El archivo `cloudbuild.yaml` construye la imagen Docker y la sube a Artifact Registry
- [ ] Cloud Run sirve la API de Spring Boot de manera pública y conectada a Cloud SQL
- [ ] El frontend completo (`index.html`) está alojado de manera pública en Firebase Hosting
- [ ] Las pruebas End-to-End demuestran que Firebase logra consumir la API de Cloud Run sin bloqueos

---

## HU-11 | Configuración de GitHub Actions para el Tablero Kanban
**Rama:** `feature/sprint3-github-actions-kanban`
**Responsable:** @Sary Ariza Vargas
**Sprint:** 3 | **Estimación:** 3 puntos | **Tipo:** DevOps | **Prioridad:** Media

### Como líder del equipo
Quiero que el tablero de GitHub Projects se gestione solo
Para reducir el trabajo manual administrativo durante los sprints

### Criterios de Aceptación:
- [ ] Existe un workflow `.github/workflows/project-automation.yml`
- [ ] El disparador (trigger) detecta cuando se asigna un usuario a un Issue
- [ ] Al detectar la asignación, la tarjeta se mueve automáticamente a la columna "In Progress"
- [ ] Al cerrar el Pull Request asociado, la tarjeta se mueve a la columna "Done"

---

## HU-12 | Redacción del README y Documentación Técnica Final
**Rama:** `feature/sprint3-documentacion`
**Responsable:** @Sary Ariza Vargas
**Sprint:** 3 | **Estimación:** 3 puntos | **Tipo:** Documentation | **Prioridad:** Alta

### Como docente evaluador
Quiero leer una documentación técnica estructurada del repositorio
Para entender la arquitectura implementada, replicar el entorno localmente y evaluar el trabajo del equipo

### Criterios de Aceptación:
- [ ] El `README.md` incluye el stack tecnológico, URLs de producción y el diagrama de arquitectura
- [ ] Contiene instrucciones explícitas para ejecutar la base de datos PostgreSQL mediante Docker
- [ ] Documenta los comandos necesarios para arrancar Spring Boot y visualizar el frontend estático localmente
- [ ] Contiene la sección "Métricas del Proyecto" y "Lecciones Aprendidas" especificadas en la rúbrica de calificación

---
