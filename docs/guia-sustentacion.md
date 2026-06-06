# Guía de sustentación — GlowLab Proyecto 3

**Duración total:** 15 minutos  
**Formato:** demostración en vivo, pasando por pantallas

---

## Antes de entrar al salón

Haz esto en tu computador antes de que empiece:

```bash
# 1. Pararte en la carpeta del proyecto
cd /ruta/a/Glowlab

# 2. Levantar el stack completo
docker compose up
```

Espera a ver `Started Application in X seconds` en los logs. Deja esa terminal abierta.

Abre estas pestañas en el navegador:
- http://localhost:8080 — la app
- http://localhost:3001 — Grafana
- https://github.com/Adrilu22/Glowlab — el repo

---

## Parte 1 — Contexto del equipo (2 min)

**Qué decir:**

> "GlowLab es una plataforma de skincare que desarrollamos en los proyectos anteriores. Ya teníamos el API REST con Spring Boot, la base de datos en PostgreSQL, el frontend y el despliegue en Google Cloud Run. En este proyecto agregamos cuatro componentes: monitoreo con Prometheus y Grafana, autenticación JWT, análisis de seguridad documentado, y un chatbot con inteligencia artificial."

**Mostrar mientras hablan:**
- El repositorio en GitHub — el historial de commits
- La app en producción: https://glowlab-api-994118614969.us-central1.run.app

---

## Parte 2 — Demostración en vivo (8 min)

### 2.1 — Autenticación JWT (2 min)

**Pantalla:** http://localhost:8080

1. Intentar hacer una acción de escritura sin login — mostrar que falla
2. Hacer login con `user@glowlab.co` / `user123`
3. Crear o editar un producto — mostrar que funciona con el token

**En otra terminal, mostrar que sin token el API devuelve 403:**
```bash
curl -X POST http://localhost:8080/api/categorias \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Test"}'
```
Sale: `403 Forbidden`

**Qué decir:**
> "Implementamos autenticación con JWT. Los endpoints de escritura — POST, PUT y DELETE — requieren un token válido en el header Authorization. Los de lectura son públicos. El token se genera al hacer login y dura 8 horas."

---

### 2.2 — Métricas en Grafana (3 min)

**Pantalla:** abrir una terminal nueva y correr el script de tráfico:

```bash
python scripts/generate_traffic.py
```

Mientras corre, abrir Grafana: http://localhost:3001  
Login: `admin` / `glowlab123`  
Ir a: Dashboards → "GlowLab API - Monitoreo"

**Qué mostrar en el dashboard:**
- Panel de throughput (requests por segundo subiendo)
- Panel de latencia P95
- Panel de tasa de errores

**Qué decir:**
> "Integramos Prometheus y Grafana con Docker Compose. La API expone un endpoint /metrics con métricas de Micrometer: contador de requests, histograma de latencia con percentiles P50, P95 y P99, y uso de memoria JVM. El dashboard se provisiona automáticamente."

---

### 2.3 — Chatbot Glow (3 min)

**Pantalla:** http://localhost:8080 — loguearse si no están logueados

1. Hacer clic en el botón flotante de chat (esquina inferior derecha)
2. Escribir: "Hola, tengo piel seca"
3. Esperar respuesta y continuar la conversación

**Qué decir:**
> "El chatbot Glow es nuestra nueva funcionalidad. Está integrado en el frontend como un asistente flotante. El backend recibe el mensaje, carga el catálogo actual de productos desde la base de datos, construye un prompt con esa información y llama a la API de Groq con el modelo Llama 3.1. El chatbot pregunta por el tipo de piel, las preocupaciones del usuario, y recomienda productos específicos del catálogo con precios en pesos colombianos."

---

## Parte 3 — Análisis de seguridad (3 min)

**Pantalla:** abrir el archivo `docs/security.md` en GitHub o en el editor

**Qué mostrar y decir:**

> "Documentamos el análisis de seguridad en security.md. Identificamos seis vulnerabilidades."

Mencionar las más importantes:

1. **Contraseñas en texto plano** — mitigada con BCrypt. Las contraseñas se hashean antes de guardarse, nunca se almacenan en texto legible.

2. **JWT sin implementación** — era el hallazgo más crítico. Lo implementamos: SecurityConfig con Spring Security, JwtUtil para generar y validar tokens, JwtAuthFilter que intercepta cada request.

3. **API key expuesta en logs** — el chatbot enviaba la clave de Gemini como query parameter, lo que la exponía en los logs de Cloud Run. La movimos al header de autorización.

4. **Sin rate limiting en login** — está documentado como pendiente. Requiere Redis o Bucket4j y queda fuera del alcance del sprint.

> "También tenemos un plan de respuesta a incidentes: si detectamos acceso no autorizado, rotamos el JWT_SECRET en Cloud Run para invalidar todos los tokens activos de inmediato."

---

## Parte 4 — Reflexión (2 min)

**Qué decir:**

> "El mayor aprendizaje fue que agregar Spring Security a un proyecto existente requiere repensar toda la configuración de CORS y los endpoints públicos. Tuvimos que ser cuidadosos con el orden de las reglas de seguridad — en Spring Security, las reglas más específicas deben ir antes que las generales."

> "La dificultad más grande fue el chatbot: empezamos con Gemini pero las claves del tier gratuito tenían cuota cero. Migramos a Groq con Llama 3.1, que es completamente gratuito y más estable para desarrollo."

> "Como equipo aprendimos a manejar secretos correctamente — GitHub bloqueó un push porque detectó una clave de API en el código, lo que nos obligó a usar variables de entorno y archivos .env excluidos del repositorio."

---

## Si te preguntan algo que no sabes

Respuesta honesta siempre funciona mejor:
> "Eso está documentado en el security.md como una medida pendiente — lo identificamos como riesgo pero quedó fuera del alcance del sprint por [razón]."

---

## Comandos de emergencia

Si algo falla durante la demo:

```bash
# Reiniciar todo
docker compose down
docker compose up

# Ver logs si hay error
docker logs glowlab-api-1

# Crear usuario si no existe
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Admin","email":"user@glowlab.co","password":"user123"}'

# Verificar que la API responde
curl http://localhost:8080/api/categorias
```
