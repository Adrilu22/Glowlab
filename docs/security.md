# Security Analysis — GlowLab API

## 1. Vulnerabilidades identificadas

### V1 — Contraseñas almacenadas en texto plano
**Riesgo:** Alto | **Estado:** Implementada

Las contraseñas de los usuarios se guardaban en la base de datos sin ningún algoritmo de hashing. Cualquier persona con acceso a la tabla `usuarios` podía leer todas las contraseñas directamente.

**Mitigación aplicada:** Se implementó `BCryptPasswordEncoder`. BCrypt es lento por diseño, lo que hace que un ataque de fuerza bruta sea computacionalmente costoso. Además genera un salt aleatorio por cada contraseña, por lo que dos usuarios con la misma clave tienen hashes completamente diferentes.

---

### V2 — Sin autenticación en endpoints de escritura
**Riesgo:** Alto | **Estado:** Implementada

Cualquier persona podía crear, editar o eliminar productos y categorías sin autenticarse. Los endpoints `POST`, `PUT` y `DELETE` eran completamente públicos.

**Mitigación aplicada:** Se implementó Spring Security con JWT. `SecurityConfig` define qué endpoints requieren token, `JwtUtil` genera y valida los tokens firmados con HS256, y `JwtAuthFilter` intercepta cada request para verificar el header `Authorization: Bearer <token>` antes de permitir el acceso.

---

### V3 — Sin limitación de intentos de login (fuerza bruta)
**Riesgo:** Medio | **Estado:** Pendiente

El endpoint `POST /api/auth/login` no tiene rate limiting ni bloqueo por intentos fallidos. Un atacante puede probar contraseñas indefinidamente sin ser bloqueado.

**Por qué está pendiente:** Requiere una dependencia adicional como Bucket4j y almacenamiento de estado (Redis o en memoria). Se excluye del alcance del sprint actual.

**Solución propuesta:** Bloquear la IP o el email tras 5 intentos fallidos consecutivos con un período de bloqueo de 15 minutos usando Bucket4j.

---

## 2. Medidas implementadas

| Vulnerabilidad | Estado | Medida aplicada |
|---|---|---|
| V1 — Contraseñas en texto plano | Implementada | `BCryptPasswordEncoder` en `AuthController` y `UsuarioController` |
| V2 — Endpoints de escritura sin autenticación | Implementada | Spring Security + JWT en `SecurityConfig`, `JwtUtil` y `JwtAuthFilter` |
| V3 — Sin rate limiting en login | Pendiente | Propuesta con Bucket4j (fuera del alcance del sprint) |

### Medidas de seguridad activas:
- **Autenticación JWT** con algoritmo HS256 y expiración de 8 horas
- **Endpoints protegidos**: `POST`, `PUT` y `DELETE` en `/api/**` requieren token válido, excepto `/api/auth/**` y `/api/chatbot`
- **Endpoints públicos**: todos los `GET` de productos y categorías son de lectura libre
- **BCrypt en contraseñas**: factor de costo 10, salt aleatorio por usuario
- **CSRF deshabilitado** correctamente para APIs REST stateless
- **CORS configurado** con lista de orígenes permitidos explícita
- **HTTPS automático en producción** a través de Google Cloud Run
- **Consultas parametrizadas** vía Spring Data JPA (previene inyección SQL)
- **Variables de entorno**: `JWT_SECRET` y `GROQ_API_KEY` se leen del entorno, no del código fuente

---

## 3. Medidas pendientes

### P1 — Rate limiting en el endpoint de login *(alta prioridad)*
**Qué falta:** Bloquear IP o email tras 5 intentos fallidos consecutivos durante 15 minutos.
**Por qué no se implementó:** Requiere Bucket4j y almacenamiento de estado, fuera del alcance del sprint.

### P2 — Proteger el endpoint `/metrics` con autenticación *(media prioridad)*
**Qué falta:** Requerir autenticación para acceder a `/metrics` desde fuera de la red interna.
**Por qué no se implementó:** En desarrollo es conveniente tenerlo público para Grafana y el navegador sin configuración extra.

### P3 — Validación de datos de entrada con Bean Validation *(media prioridad)*
**Qué falta:** Agregar `@Valid`, `@NotNull`, `@Email`, `@Positive` a los modelos.
**Por qué no se implementó:** Requiere reestructurar los controladores para usar DTOs en lugar de entidades directamente.

---

## 4. Plan de respuesta a incidentes

### Escenario: acceso no autorizado detectado

1. **Detección**: El equipo detecta requests anómalos en Grafana (picos de tráfico, errores 401/403 masivos) o actividad sospechosa en los logs de Cloud Run.

2. **Contención inmediata**:
   - Rotar el `JWT_SECRET` en las variables de entorno de Cloud Run → todos los tokens activos quedan invalidados de inmediato sin necesidad de reiniciar el servicio.
   - Suspender temporalmente el endpoint de login si se detecta un ataque de fuerza bruta.

3. **Evaluación del daño**:
   - Revisar los logs de acceso para identificar qué endpoints fueron accedidos y qué datos fueron leídos o modificados.
   - Verificar la integridad de la base de datos (usuarios, productos, categorías).

4. **Recuperación**:
   - Restaurar datos desde el último backup si se detectaron modificaciones no autorizadas.
   - Forzar el cambio de contraseña de los usuarios afectados.
   - Notificar a los usuarios si se comprometieron datos personales (email, nombre).

5. **Post-incidente**:
   - Implementar rate limiting (P1) como prioridad inmediata.
   - Documentar el incidente y las acciones tomadas.
