# GlowLab — Documentación de la API REST

**Versión:** 1.0  
**Base URL (producción):** `https://glowlab-api-994118614969.us-central1.run.app`  
**Base URL (local):** `http://localhost:8080`  
**Formato:** JSON (`Content-Type: application/json`)  
**Autenticación:** No requerida en esta versión

---

## Índice de Recursos

| Recurso | Base path | Descripción |
|---|---|---|
| [Categorías](#categorías) | `/api/categorias` | Tipos de productos de skincare |
| [Productos](#productos) | `/api/productos` | Catálogo de productos |
| [Compras](#compras) | `/api/compras` | Registro de compras del carrito |
| [Usuarios](#usuarios) | `/api/usuarios` | Cuentas de usuario |
| [Rutinas](#rutinas) | `/api/rutinas` | Rutinas de cuidado personalizadas |

---

## Códigos de Estado HTTP

| Código | Significado |
|---|---|
| `200 OK` | Petición exitosa |
| `201 Created` | Recurso creado (algunos endpoints devuelven 200 con el recurso) |
| `204 No Content` | Eliminación exitosa, sin cuerpo de respuesta |
| `400 Bad Request` | Datos de entrada inválidos o regla de negocio violada |
| `404 Not Found` | El recurso con ese ID no existe |
| `500 Internal Server Error` | Error no controlado en el servidor |

---

## Categorías

Representa las categorías de productos (Limpieza, Hidratación, Protección, etc.).

### Esquema

```json
{
  "id":          1,
  "nombre":      "Limpieza",
  "descripcion": "Productos para limpiar y purificar el rostro",
  "icono":       "🧼"
}
```

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `id` | Long | — (generado) | Identificador único |
| `nombre` | String | Sí | Nombre de la categoría |
| `descripcion` | String | No | Descripción larga |
| `icono` | String | No | Emoji o código de icono |

---

### GET /api/categorias

Devuelve todas las categorías.

**Request**
```
GET /api/categorias
```

**Response 200 OK**
```json
[
  {
    "id": 1,
    "nombre": "Limpieza",
    "descripcion": "Productos para limpiar y purificar el rostro",
    "icono": "🧼"
  },
  {
    "id": 2,
    "nombre": "Hidratación",
    "descripcion": "Cremas y sueros hidratantes",
    "icono": "💧"
  },
  {
    "id": 3,
    "nombre": "Protección Solar",
    "descripcion": "Protectores con SPF 30 y SPF 50",
    "icono": "☀️"
  }
]
```

**Ejemplo con curl:**
```bash
curl https://glowlab-api-994118614969.us-central1.run.app/api/categorias
```

---

### GET /api/categorias/{id}

Devuelve una categoría por su ID.

**Request**
```
GET /api/categorias/1
```

**Response 200 OK**
```json
{
  "id": 1,
  "nombre": "Limpieza",
  "descripcion": "Productos para limpiar y purificar el rostro",
  "icono": "🧼"
}
```

**Response 404 Not Found** — si el ID no existe
```json
(sin cuerpo)
```

---

### POST /api/categorias

Crea una nueva categoría.

**Request**
```
POST /api/categorias
Content-Type: application/json
```
```json
{
  "nombre": "Exfoliación",
  "descripcion": "Exfoliantes físicos y químicos",
  "icono": "✨"
}
```

**Response 200 OK** — devuelve el recurso creado con su ID
```json
{
  "id": 6,
  "nombre": "Exfoliación",
  "descripcion": "Exfoliantes físicos y químicos",
  "icono": "✨"
}
```

**Ejemplo con curl:**
```bash
curl -X POST https://glowlab-api-994118614969.us-central1.run.app/api/categorias \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Exfoliación","descripcion":"Exfoliantes físicos y químicos","icono":"✨"}'
```

---

### PUT /api/categorias/{id}

Actualiza una categoría existente. Reemplaza los campos `nombre`, `descripcion` e `icono`.

**Request**
```
PUT /api/categorias/6
Content-Type: application/json
```
```json
{
  "nombre": "Exfoliación Profunda",
  "descripcion": "Exfoliantes de uso semanal",
  "icono": "🌿"
}
```

**Response 200 OK**
```json
{
  "id": 6,
  "nombre": "Exfoliación Profunda",
  "descripcion": "Exfoliantes de uso semanal",
  "icono": "🌿"
}
```

**Response 404 Not Found** — si el ID no existe

---

### DELETE /api/categorias/{id}

Elimina una categoría por su ID.

**Request**
```
DELETE /api/categorias/6
```

**Response 204 No Content** — eliminación exitosa, sin cuerpo  
**Response 404 Not Found** — si el ID no existe

---

## Productos

Catálogo de productos de skincare. Cada producto pertenece a una categoría.

### Esquema

```json
{
  "id":          1,
  "nombre":      "Gel Limpiador Cetaphil",
  "marca":       "Cetaphil",
  "descripcion": "Limpiador suave para todo tipo de piel",
  "precio":      35000,
  "tiposPiel":   ["seca", "mixta", "sensible"],
  "categoria": {
    "id":          1,
    "nombre":      "Limpieza",
    "descripcion": "Productos para limpiar el rostro",
    "icono":       "🧼"
  }
}
```

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `id` | Long | — (generado) | Identificador único |
| `nombre` | String | Sí | Nombre del producto |
| `marca` | String | No | Marca comercial |
| `descripcion` | String | No | Descripción del producto |
| `precio` | double | Sí | Precio en pesos colombianos |
| `tiposPiel` | String[] | No | Tipos de piel compatibles (response es array; request acepta CSV o array) |
| `categoria` | Objeto | Sí | Objeto con `id` de la categoría |

> **Nota técnica:** `tiposPiel` se almacena en base de datos como un string CSV (`"seca,mixta,grasa"`). El endpoint de respuesta lo convierte automáticamente a un array JSON de strings.

---

### GET /api/productos

Devuelve todos los productos. Acepta filtrado opcional por categoría.

**Request**
```
GET /api/productos
GET /api/productos?categoriaId=1
```

**Query params:**

| Parámetro | Tipo | Requerido | Descripción |
|---|---|---|---|
| `categoriaId` | Long | No | Filtra productos de esa categoría |

**Response 200 OK**
```json
[
  {
    "id": 1,
    "nombre": "Gel Limpiador Cetaphil",
    "marca": "Cetaphil",
    "descripcion": "Limpiador suave para todo tipo de piel",
    "precio": 35000,
    "tiposPiel": ["seca", "mixta", "sensible"],
    "categoria": {
      "id": 1,
      "nombre": "Limpieza",
      "descripcion": "Productos para limpiar el rostro",
      "icono": "🧼"
    }
  },
  {
    "id": 2,
    "nombre": "Sérum Vitamina C",
    "marca": "The Ordinary",
    "descripcion": "Sérum antioxidante con 10% vitamina C",
    "precio": 65000,
    "tiposPiel": ["normal", "mixta"],
    "categoria": {
      "id": 4,
      "nombre": "Tratamiento",
      "descripcion": null,
      "icono": null
    }
  }
]
```

**Ejemplo con curl:**
```bash
# Todos los productos
curl https://glowlab-api-994118614969.us-central1.run.app/api/productos

# Solo productos de la categoría 1 (Limpieza)
curl "https://glowlab-api-994118614969.us-central1.run.app/api/productos?categoriaId=1"
```

---

### GET /api/productos/{id}

Devuelve un producto por su ID.

**Response 200 OK**
```json
{
  "id": 1,
  "nombre": "Gel Limpiador Cetaphil",
  "marca": "Cetaphil",
  "descripcion": "Limpiador suave para todo tipo de piel",
  "precio": 35000,
  "tiposPiel": ["seca", "mixta", "sensible"],
  "categoria": {
    "id": 1,
    "nombre": "Limpieza",
    "descripcion": "Productos para limpiar el rostro",
    "icono": "🧼"
  }
}
```

**Response 404 Not Found** — si el ID no existe

---

### GET /api/productos/{id}/categoria

Devuelve únicamente la categoría del producto indicado.

**Response 200 OK**
```json
{
  "id": 1,
  "nombre": "Limpieza",
  "descripcion": "Productos para limpiar el rostro",
  "icono": "🧼"
}
```

---

### POST /api/productos

Crea un nuevo producto. La categoría debe existir previamente.

**Request**
```
POST /api/productos
Content-Type: application/json
```
```json
{
  "nombre": "Mascarilla de Arcilla",
  "marca": "L'Oréal",
  "descripcion": "Mascarilla purificante para piel grasa",
  "precio": 48000,
  "tiposPiel": "grasa,mixta",
  "categoria": {
    "id": 1
  }
}
```

**Response 200 OK** — producto creado
```json
{
  "id": 7,
  "nombre": "Mascarilla de Arcilla",
  "marca": "L'Oréal",
  "descripcion": "Mascarilla purificante para piel grasa",
  "precio": 48000,
  "tiposPiel": ["grasa", "mixta"],
  "categoria": {
    "id": 1,
    "nombre": "Limpieza",
    "descripcion": "Productos para limpiar el rostro",
    "icono": "🧼"
  }
}
```

**Response 400 Bad Request** — si la categoría no existe o no se envía
```
La categoría con id 99 no existe
```
```
La categoría es obligatoria
```

**Ejemplo con curl:**
```bash
curl -X POST https://glowlab-api-994118614969.us-central1.run.app/api/productos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Mascarilla de Arcilla",
    "marca": "L'\''Oréal",
    "precio": 48000,
    "tiposPiel": "grasa,mixta",
    "categoria": {"id": 1}
  }'
```

---

### PUT /api/productos/{id}

Actualiza un producto existente. Todos los campos se reemplazan.

**Request**
```
PUT /api/productos/7
Content-Type: application/json
```
```json
{
  "nombre": "Mascarilla de Arcilla Premium",
  "marca": "L'Oréal",
  "descripcion": "Mascarilla purificante con carbón activado",
  "precio": 55000,
  "tiposPiel": "grasa",
  "categoria": {
    "id": 1
  }
}
```

**Response 200 OK** — producto actualizado  
**Response 400 Bad Request** — categoría inválida  
**Response 404 Not Found** — ID no existe

---

### DELETE /api/productos/{id}

Elimina un producto por su ID.

**Response 204 No Content** — eliminación exitosa  
**Response 404 Not Found** — si el ID no existe

---

## Compras

Registra una compra completa (cabecera + detalle de productos) en una sola llamada.

### POST /api/compras

Crea una compra y registra todos sus productos. Recibe un array con los IDs de productos del carrito.

**Request**
```
POST /api/compras
Content-Type: application/json
```
```json
[
  { "productoId": 1 },
  { "productoId": 3 },
  { "productoId": 3 }
]
```

> Se puede repetir el mismo `productoId` si el usuario agregó más de una unidad de un producto.

**Response 200 OK**
```
Compra guardada correctamente ✅
```

**Response 500 Internal Server Error** — si hay un error al persistir
```
Error: <mensaje de la excepción>
```

**Ejemplo con curl:**
```bash
curl -X POST https://glowlab-api-994118614969.us-central1.run.app/api/compras \
  -H "Content-Type: application/json" \
  -d '[{"productoId": 1}, {"productoId": 3}]'
```

**Cómo se persiste en la base de datos:**

Una sola llamada a `POST /api/compras` crea:
1. Un registro en la tabla `compra` (con `id` y `fecha` automáticos)
2. Un registro en `detalle_compra` por cada elemento del array, vinculado al `compra.id`

---

## Usuarios

Gestión de cuentas de usuario. Los usuarios se registran con nombre, email y contraseña.

### Esquema

```json
{
  "id":           1,
  "nombre":       "Ana García",
  "email":        "ana@email.com",
  "passwordHash": "admin123",
  "rol":          "user",
  "createdAt":    "2026-04-21T10:30:00"
}
```

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `id` | Long | — (generado) | Identificador único |
| `nombre` | String | Sí | Nombre completo |
| `email` | String | Sí | Email único, se valida duplicado |
| `passwordHash` | String | No | Contraseña (en texto plano en esta versión) |
| `rol` | String | No | `"user"` (defecto) o `"admin"` |
| `createdAt` | LocalDateTime | — (generado) | Fecha de creación |

---

### GET /api/usuarios

Devuelve todos los usuarios registrados.

**Response 200 OK**
```json
[
  {
    "id": 1,
    "nombre": "Ana García",
    "email": "ana@email.com",
    "passwordHash": "admin123",
    "rol": "user",
    "createdAt": "2026-04-21T10:30:00"
  },
  {
    "id": 2,
    "nombre": "Admin GlowLab",
    "email": "admin@glowlab.co",
    "passwordHash": "admin123",
    "rol": "admin",
    "createdAt": "2026-04-20T09:00:00"
  }
]
```

---

### GET /api/usuarios/{id}

Devuelve un usuario por su ID.

**Response 200 OK** — usuario encontrado  
**Response 404 Not Found** — ID no existe

---

### POST /api/usuarios

Registra un nuevo usuario. Valida que el email no esté duplicado.

**Request**
```
POST /api/usuarios
Content-Type: application/json
```
```json
{
  "nombre": "María López",
  "email": "maria@email.com",
  "passwordHash": "mipassword123",
  "rol": "user"
}
```

**Response 200 OK** — usuario creado
```json
{
  "id": 3,
  "nombre": "María López",
  "email": "maria@email.com",
  "passwordHash": "mipassword123",
  "rol": "user",
  "createdAt": "2026-04-21T14:00:00"
}
```

**Response 400 Bad Request** — email ya registrado
```
Ya existe un usuario con ese correo
```

**Ejemplo con curl:**
```bash
curl -X POST https://glowlab-api-994118614969.us-central1.run.app/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "María López",
    "email": "maria@email.com",
    "passwordHash": "mipassword123",
    "rol": "user"
  }'
```

---

### PUT /api/usuarios/{id}

Actualiza nombre, email y rol de un usuario. Si se envía `passwordHash` no vacío, también se actualiza la contraseña.

**Request**
```
PUT /api/usuarios/3
Content-Type: application/json
```
```json
{
  "nombre": "María L. Actualizada",
  "email": "maria.nueva@email.com",
  "rol": "admin",
  "passwordHash": ""
}
```

> Si `passwordHash` es vacío o `null`, la contraseña existente no se modifica.

**Response 200 OK** — usuario actualizado  
**Response 404 Not Found** — ID no existe

---

### DELETE /api/usuarios/{id}

Elimina un usuario por su ID.

**Response 204 No Content** — eliminación exitosa  
**Response 404 Not Found** — si el ID no existe

---

## Rutinas

Rutinas de cuidado de piel personalizadas. Cada rutina está asociada a un usuario y contiene los pasos como JSON serializado.

### Esquema

```json
{
  "id":             1,
  "usuarioId":      2,
  "nombre":         "Rutina piel seca - mañana",
  "tipoPiel":       "seca",
  "preocupaciones": "manchas,deshidratación",
  "pasosJson":      "[{\"paso\":1,\"descripcion\":\"Limpiar con gel suave\"},{\"paso\":2,\"descripcion\":\"Aplicar sérum hidratante\"}]",
  "createdAt":      "2026-04-21T08:00:00"
}
```

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `id` | Long | — (generado) | Identificador único |
| `usuarioId` | Long | No | ID del usuario dueño de la rutina |
| `nombre` | String | No | Nombre descriptivo de la rutina |
| `tipoPiel` | String | No | Tipo de piel: `"seca"`, `"grasa"`, `"mixta"`, `"normal"`, `"sensible"` |
| `preocupaciones` | String | No | Preocupaciones de piel (texto libre) |
| `pasosJson` | String | No | Pasos de la rutina en formato JSON serializado como string |
| `createdAt` | LocalDateTime | — (generado) | Fecha de creación |

---

### GET /api/rutinas

Devuelve todas las rutinas registradas.

**Response 200 OK**
```json
[
  {
    "id": 1,
    "usuarioId": 2,
    "nombre": "Rutina piel seca - mañana",
    "tipoPiel": "seca",
    "preocupaciones": "manchas,deshidratación",
    "pasosJson": "[{\"paso\":1,\"descripcion\":\"Limpiar con gel suave\"}]",
    "createdAt": "2026-04-21T08:00:00"
  }
]
```

---

### GET /api/rutinas/usuario/{usuarioId}

Devuelve todas las rutinas de un usuario específico.

**Request**
```
GET /api/rutinas/usuario/2
```

**Response 200 OK** — array de rutinas del usuario (vacío si no tiene)
```json
[
  {
    "id": 1,
    "usuarioId": 2,
    "nombre": "Rutina piel seca - mañana",
    "tipoPiel": "seca",
    "preocupaciones": "manchas,deshidratación",
    "pasosJson": "...",
    "createdAt": "2026-04-21T08:00:00"
  }
]
```

---

### GET /api/rutinas/{id}

Devuelve una rutina por su ID.

**Response 200 OK** — rutina encontrada  
**Response 404 Not Found** — ID no existe

---

### POST /api/rutinas

Registra una nueva rutina de cuidado.

**Request**
```
POST /api/rutinas
Content-Type: application/json
```
```json
{
  "usuarioId": 2,
  "nombre": "Rutina piel mixta - noche",
  "tipoPiel": "mixta",
  "preocupaciones": "poros dilatados, zonas brillantes",
  "pasosJson": "[{\"paso\":1,\"descripcion\":\"Doble limpieza con aceite y gel\"},{\"paso\":2,\"descripcion\":\"Tónico sin alcohol\"},{\"paso\":3,\"descripcion\":\"Crema ligera oil-free\"}]"
}
```

**Response 200 OK** — rutina creada
```json
{
  "id": 2,
  "usuarioId": 2,
  "nombre": "Rutina piel mixta - noche",
  "tipoPiel": "mixta",
  "preocupaciones": "poros dilatados, zonas brillantes",
  "pasosJson": "[{\"paso\":1,\"descripcion\":\"Doble limpieza con aceite y gel\"}...]",
  "createdAt": "2026-04-21T22:00:00"
}
```

**Ejemplo con curl:**
```bash
curl -X POST https://glowlab-api-994118614969.us-central1.run.app/api/rutinas \
  -H "Content-Type: application/json" \
  -d '{
    "usuarioId": 2,
    "nombre": "Rutina piel mixta - noche",
    "tipoPiel": "mixta",
    "preocupaciones": "poros dilatados",
    "pasosJson": "[{\"paso\":1,\"descripcion\":\"Limpiar\"}]"
  }'
```

---

### DELETE /api/rutinas/{id}

Elimina una rutina por su ID.

**Response 204 No Content** — eliminación exitosa  
**Response 404 Not Found** — si el ID no existe

---

## Resumen de Endpoints

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/categorias` | Listar todas las categorías |
| GET | `/api/categorias/{id}` | Obtener categoría por ID |
| POST | `/api/categorias` | Crear categoría |
| PUT | `/api/categorias/{id}` | Actualizar categoría |
| DELETE | `/api/categorias/{id}` | Eliminar categoría |
| GET | `/api/productos` | Listar productos (con filtro opcional `?categoriaId=`) |
| GET | `/api/productos/{id}` | Obtener producto por ID |
| GET | `/api/productos/{id}/categoria` | Obtener categoría de un producto |
| POST | `/api/productos` | Crear producto |
| PUT | `/api/productos/{id}` | Actualizar producto |
| DELETE | `/api/productos/{id}` | Eliminar producto |
| POST | `/api/compras` | Registrar compra con sus productos |
| GET | `/api/usuarios` | Listar todos los usuarios |
| GET | `/api/usuarios/{id}` | Obtener usuario por ID |
| POST | `/api/usuarios` | Registrar usuario |
| PUT | `/api/usuarios/{id}` | Actualizar usuario |
| DELETE | `/api/usuarios/{id}` | Eliminar usuario |
| GET | `/api/rutinas` | Listar todas las rutinas |
| GET | `/api/rutinas/usuario/{usuarioId}` | Listar rutinas de un usuario |
| GET | `/api/rutinas/{id}` | Obtener rutina por ID |
| POST | `/api/rutinas` | Crear rutina |
| DELETE | `/api/rutinas/{id}` | Eliminar rutina |

---

## Configuración CORS

La API acepta solicitudes de los siguientes orígenes:

```
http://localhost:3000
http://localhost:5500
http://127.0.0.1:5500
https://glowlab-api-994118614969.us-central1.run.app
```

Para agregar un nuevo origen, modificar `cors.allowed-origins` en `application.properties` y hacer deploy.

---

## Notas de Implementación

- **Conexión a BD:** Cloud SQL Socket Factory via Unix socket. No se usa TCP directo.
- **ORM:** Hibernate con `ddl-auto=update`. Las tablas se crean/actualizan automáticamente al iniciar la aplicación.
- **Frontend embebido:** El frontend HTML/CSS/JS se sirve como recurso estático desde el mismo contenedor en `/`.
- **Pool de conexiones:** HikariCP configurado con máximo 2 conexiones (`hikari.maximum-pool-size=2`) para optimizar el uso dentro del tier gratuito de Cloud SQL.
- **Contraseña de BD:** Inyectada desde Secret Manager via `--set-secrets=DB_PASSWORD=db-password:latest` en el paso de deploy de Cloud Build.
