# Security Analysis — GlowLab API

## 1. Vulnerabilidades identificadas

### V1 — Contraseñas almacenadas en texto plano
Las contraseñas de los usuarios se guardan en la columna `password_hash` de la tabla `usuarios` sin ningún algoritmo de hashing. Cualquier persona con acceso a la base de datos puede leer todas las contraseñas directamente.

### V2 — JWT secret embebido en el código fuente
La clave secreta usada para firmar los tokens JWT (`jwt.secret`) está definida directamente en `application.properties` y versionada en el repositorio. Si el repositorio es público o es comprometido, todos los tokens generados pueden ser falsificados.

### V3 — Sin limitación de intentos de login (fuerza bruta)
El endpoint `POST /api/auth/login` no tiene rate limiting ni bloqueo por intentos fallidos. Un atacante puede probar contraseñas indefinidamente sin ser bloqueado.

### V4 — Endpoint `/metrics` expuesto públicamente
El endpoint `http://localhost:8080/metrics` no requiere autenticación y expone información interna del servidor: uso de memoria JVM, cantidad de requests, tiempos de respuesta y rutas de la API. Esta información puede ser usada para planear ataques.

### V5 — Tokens JWT sin posibilidad de revocación
Una vez emitido un JWT, no puede invalidarse antes de su expiración (15 minutos). Si el token es robado, el atacante tiene acceso durante todo ese tiempo sin forma de bloquearlo.

### V6 — Sin validación de input en los controladores
Los endpoints no validan el formato ni el contenido de los datos recibidos (ej. email válido, precio positivo, campos obligatorios). Esto puede generar errores internos o datos corruptos en la base de datos.

---

## 2. Medidas implementadas

| Vulnerabilidad | Medida aplicada |
|---|---|
| V1 — Contraseñas en texto plano | Se identificó el riesgo. Pendiente de implementar BCrypt (ver sección 3). |
| V2 — JWT secret en código | La clave puede sobreescribirse con la variable de entorno `JWT_SECRET` en producción (Cloud Run). En Docker local se usa el valor del archivo. |
| V3 — Fuerza bruta en login | Tokens de corta duración (15 min) reducen la ventana de ataque. Pendiente rate limiting. |
| V4 — Métricas expuestas | En producción (Cloud Run) el acceso a `/metrics` puede restringirse por red. Localmente es intencional para desarrollo. |
| V5 — Tokens sin revocación | Se configuró expiración de 15 minutos para minimizar el impacto de un token robado. |
| V6 — Sin validación de input | Spring Data JPA usa consultas parametrizadas, previniendo inyección SQL. Pendiente validación de formato. |

### Medidas de seguridad activas:
- **Autenticación JWT** con algoritmo HS256 y expiración de 15 minutos. Implementada en `SecurityConfig.java`, `JwtUtil.java` y `JwtAuthFilter.java`.
- **Endpoints protegidos**: todos los métodos `POST`, `PUT` y `DELETE` en `/api/**` requieren token válido en el header `Authorization: Bearer <token>`, excepto `/api/auth/**`, `/api/usuarios` (POST) y `/api/chatbot` (POST).
- **BCrypt en contraseñas**: `UsuarioController` y `AuthController` usan `BCryptPasswordEncoder` antes de guardar cualquier contraseña.
- **API key de Gemini en header**: `ChatbotService` envía la clave en el header `x-goog-api-key` en lugar de query param, evitando que aparezca en logs de Cloud Run.
- **CSRF deshabilitado** correctamente para APIs REST stateless.
- **CORS configurado** con lista de orígenes permitidos explícita.
- **HTTPS automático en producción** a través de Google Cloud Run.
- **Consultas parametrizadas** vía Spring Data JPA (previene inyección SQL).

---

## 3. Medidas pendientes

### P1 — Hashear contraseñas con BCrypt *(implementado)*
`BCryptPasswordEncoder` está activo en `UsuarioController` y `AuthController`. Las contraseñas nuevas y actualizaciones se hashean antes de guardarse. Las contraseñas ya existentes en la BD de prueba deben recrearse para funcionar con el nuevo flujo de login.

### P2 — Rate limiting en el endpoint de login *(alta prioridad)*
**Qué falta:** Bloquear la IP o el email tras N intentos fallidos consecutivos (ej. 5 intentos → bloqueo de 15 min).  
**Por qué no se implementó:** Requiere una dependencia adicional (Bucket4j o Spring Security's rate limiter) y almacenamiento de estado (Redis o en memoria). Se excluye del alcance del sprint actual.

### P3 — Proteger el endpoint `/metrics` con autenticación *(media prioridad)*
**Qué falta:** Requerir un token de Prometheus o restringir el acceso por IP/red interna.  
**Por qué no se implementó:** En el entorno de desarrollo es conveniente tenerlo público para acceder desde Grafana y el navegador sin configuración extra.

### P4 — Validación de datos de entrada con Bean Validation *(media prioridad)*
**Qué falta:** Agregar `@Valid`, `@NotNull`, `@Email`, `@Positive` a los DTOs y modelos.  
**Por qué no se implementó:** Requiere reestructurar los controladores para usar DTOs en lugar de entidades directamente.

### P5 — Refresh tokens *(baja prioridad)*
**Qué falta:** Implementar un mecanismo de refresh token para renovar el acceso sin re-login.  
**Por qué no se implementó:** Aumenta la complejidad de la implementación y el alcance del proyecto.

---

## 4. Plan de respuesta a incidentes

### Escenario: acceso no autorizado detectado

1. **Detección**: El equipo detecta requests anómalos en Grafana (picos de tráfico, errores 401/403 masivos) o recibe alerta de actividad sospechosa en los logs de Cloud Run.

2. **Contención inmediata**:
   - Rotar el `JWT_SECRET` en las variables de entorno de Cloud Run → todos los tokens activos quedan invalidados de inmediato.
   - Suspender temporalmente el endpoint de login si se detecta un ataque de fuerza bruta.

3. **Evaluación del daño**:
   - Revisar los logs de acceso para identificar qué endpoints fueron accedidos y qué datos fueron leídos o modificados.
   - Verificar la integridad de la base de datos (usuarios, productos, compras).

4. **Recuperación**:
   - Restaurar datos desde el último backup si se detectaron modificaciones no autorizadas.
   - Forzar el cambio de contraseña de los usuarios afectados.
   - Notificar a los usuarios si se comprometieron datos personales (email, nombre).

5. **Post-incidente**:
   - Implementar las medidas pendientes priorizadas (BCrypt, rate limiting).
   - Documentar el incidente y las acciones tomadas.
