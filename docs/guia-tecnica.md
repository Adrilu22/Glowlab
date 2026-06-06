# Guía técnica — Preguntas frecuentes en sustentación

---

## JWT — Autenticación

**¿Qué es JWT?**
JSON Web Token. Es un string codificado en base64 que contiene el email del usuario, su rol y una fecha de expiración, firmado con una clave secreta. El servidor no necesita guardar sesiones — valida el token en cada request comparando la firma.

**¿Cómo funciona el flujo completo?**
1. El usuario hace POST `/api/auth/login` con email y contraseña
2. El backend verifica la contraseña con BCrypt
3. Si es correcta, genera un token JWT firmado con HS256
4. El frontend guarda ese token en localStorage
5. En cada request siguiente, el frontend lo manda en el header: `Authorization: Bearer <token>`
6. `JwtAuthFilter` intercepta el request, valida el token, y si es válido registra al usuario en el SecurityContext de Spring

**¿Por qué HS256?**
Es el algoritmo HMAC con SHA-256. Usa una sola clave compartida para firmar y verificar. Es suficiente para este proyecto — la alternativa sería RS256 con clave pública/privada, que es más complejo.

**¿Qué pasa si el token expira?**
El filtro lanza una excepción, Spring Security rechaza el request con 401 o 403. El usuario tiene que volver a hacer login. En este proyecto el token dura 8 horas.

**¿Por qué no se puede revocar un JWT?**
Porque es stateless — el servidor no guarda registro de tokens emitidos. Para invalidarlo antes de que expire habría que mantener una blacklist en Redis. Está documentado como medida pendiente (P5) en security.md.

---

## Spring Security

**¿Qué hace SecurityConfig?**
Define las reglas de acceso: qué endpoints son públicos y cuáles requieren token. También desactiva CSRF (correcto para APIs REST stateless) y configura la sesión como STATELESS.

**¿Por qué se desactiva CSRF?**
CSRF protege formularios HTML donde el navegador envía cookies automáticamente. En una API REST con tokens JWT en headers, el navegador no puede hacer requests maliciosos automáticamente — el atacante necesita el token. CSRF no aplica.

**¿Qué es JwtAuthFilter?**
Un `OncePerRequestFilter` de Spring — se ejecuta una vez por cada request HTTP. Lee el header Authorization, extrae el token Bearer, lo valida con JwtUtil, y si es válido registra al usuario en el SecurityContext para que Spring lo trate como autenticado.

---

## BCrypt

**¿Por qué BCrypt y no MD5 o SHA-256?**
MD5 y SHA-256 son rápidos — un atacante puede probar millones de contraseñas por segundo con hardware moderno. BCrypt es intencionalmente lento y tiene un "work factor" configurable. También genera un salt aleatorio en cada hash, por lo que dos usuarios con la misma contraseña tienen hashes diferentes.

**¿Cómo funciona la verificación?**
`passwordEncoder.matches(rawPassword, hashedPassword)` extrae el salt del hash guardado, aplica BCrypt a la contraseña ingresada con ese mismo salt, y compara los resultados.

---

## Prometheus y Grafana

**¿Qué es Prometheus?**
Un sistema de monitoreo que recolecta métricas haciendo scraping (peticiones GET periódicas) a los endpoints `/metrics` de las aplicaciones. Guarda las métricas en una base de datos de series de tiempo.

**¿Cómo sabe Prometheus a qué URLs hacer scraping?**
Lo define `monitoring/prometheus.yml`. En nuestro caso: `targets: ["api:8080"]` con `metrics_path: "/metrics"`. Cada 15 segundos hace GET a http://api:8080/metrics.

**¿Qué es Micrometer?**
La librería de Java que expone las métricas de la aplicación. Spring Boot Actuator + Micrometer Prometheus las formatea en el estándar que Prometheus entiende.

**¿Qué métricas expone la API?**
- `http_server_requests_seconds_count` — número de requests por endpoint y código HTTP
- `http_server_requests_seconds` — histograma de latencia (P50, P95, P99)
- `jvm_memory_used_bytes` — uso de memoria de la JVM (gauge)

**¿Qué es Grafana?**
Una herramienta de visualización. Se conecta a Prometheus como datasource y permite crear dashboards con gráficas. Nuestro dashboard `glowlab.json` se provisiona automáticamente al iniciar el contenedor.

**¿Por qué no está en la nube?**
El profesor lo permite explícitamente. Desplegar Prometheus y Grafana en Cloud Run requeriría configurar almacenamiento persistente, networking entre servicios, y autenticación adicional. Para este proyecto corre localmente y se conecta a la API local.

---

## Chatbot

**¿Cómo funciona el chatbot?**
1. El frontend envía `POST /api/chatbot` con el mensaje y el historial de la conversación
2. `ChatbotService` carga todos los productos de la BD
3. Construye un system prompt que incluye el catálogo completo con nombres, marcas, precios y tipos de piel
4. Llama a la API de Groq con el modelo Llama 3.1 8B Instant
5. Devuelve la respuesta al frontend

**¿Por qué Groq y no Gemini?**
Gemini tiene restricciones de cuota en el tier gratuito que causaron problemas durante el desarrollo. Groq ofrece acceso gratuito a modelos open source (Llama 3.1) sin límite diario y con menor latencia.

**¿Qué es el tag [ASESOR_DISPONIBLE]?**
Es una instrucción en el system prompt. Si el modelo detecta que el usuario necesita asesoría personalizada, incluye ese tag en su respuesta. El frontend lo detecta, lo elimina del texto visible, y muestra un botón de "Hablar con un asesor".

**¿Por qué se carga el catálogo en cada request?**
Para que el chatbot siempre tenga información actualizada. Si se agrega un producto nuevo, el chatbot lo conoce inmediatamente en el siguiente mensaje.

---

## Vulnerabilidades de seguridad

**¿Qué es inyección SQL y por qué no aplica aquí?**
Inyección SQL ocurre cuando un atacante mete código SQL en un campo de formulario. Spring Data JPA usa consultas parametrizadas — los valores del usuario nunca se concatenan directamente en el SQL, se pasan como parámetros seguros.

**¿Qué es CORS y por qué lo configuramos?**
Cross-Origin Resource Sharing. Los navegadores bloquean requests de un dominio a otro por defecto. Nuestro frontend en Cloud Run hace requests al API en el mismo dominio, pero durante desarrollo el frontend local (localhost:5500) necesita acceder al API (localhost:8080). `WebConfig.java` define la lista de orígenes permitidos.

**¿Qué haría el equipo si detecta acceso no autorizado?**
Está en security.md sección 4:
1. Rotar el `JWT_SECRET` en las variables de entorno de Cloud Run — invalida todos los tokens activos
2. Revisar los logs de Cloud Run para identificar qué endpoints fueron accedidos
3. Verificar integridad de la base de datos
4. Notificar a usuarios afectados si se comprometieron datos personales

---

## Docker y despliegue

**¿Qué hace el Dockerfile multi-stage?**
Etapa 1: compila el proyecto con Maven y JDK 21 — genera el JAR.
Etapa 2: copia solo el JAR en una imagen ligera con JRE Alpine (~200 MB vs ~600 MB). La imagen final no tiene Maven ni el código fuente.

**¿Por qué un solo contenedor sirve el frontend y el API?**
El frontend (HTML/CSS/JS) está en `src/main/resources/static/`. Spring Boot sirve automáticamente los archivos estáticos desde ahí. Un solo contenedor, una sola URL, sin CORS entre dominios.

**¿Qué es Cloud SQL Auth Proxy?**
Un proxy que crea una conexión segura entre Cloud Run y Cloud SQL sin exponer la base de datos a internet. En producción corre como sidecar. En local se reemplaza con una conexión directa TCP (docker-compose conecta el API al contenedor de PostgreSQL por hostname `db`).
