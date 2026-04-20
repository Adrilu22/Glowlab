📖 Documentación de la API de Skincare
🚀 Base URL
https://api-skincare-v2-994118614969.us-central1.run.app/api/categorias
https://api-skincare-v2-994118614969.us-central1.run.app/api/productos

📑 Lista de Endpoints
1. Obtener todos los productos
Método: GET

URL: /api/productos

Response (200 OK):

JSON
[
  {
    "id": 2,
    "nombre": "Gel limpiador",
    "precio": 25000,
    "categoriaId": 1
  },
  {
    "id": 3,
    "nombre": "Crema hidratante",
    "precio": 30000,
    "categoriaId": 2
  }
]
2. Crear un nuevo producto
Método: POST

URL: /api/productos

Request Body:

JSON
{
  "nombre": "Mascarilla facial",
  "precio": 40000,
  "categoria": {
    "id": 2
  }
}
Response (201 Created): Retorna el objeto creado con su ID.

3. Actualizar producto
Método: PUT

URL: /api/productos/{id}

JSON
{
  "nombre": "Serum vitamina C",
  "precio": 65000,
  "categoria": {
    "id": 2
  }
}

Response (200 OK): Producto actualizado correctamente.

4. Eliminar producto
Método: DELETE

URL: /api/productos/{id}

Response (204 No Content): Éxito sin contenido.

🧾 1. Obtener todos los categorias
Método: GET
URL: /api/categorias

JSON
[
  { "id": 1, "nombre": "Limpieza" },
  { "id": 2, "nombre": "Hidratacion" },
  { "id": 3, "nombre": "Proteccion" },
  { "id": 4, "nombre": "Tratamiento" },
  { "id": 5, "nombre": "Tonificacion" }
]
Response:200 OK: Petición exitosa.

2. Crear una nueva categoría

Método: POST
URL: /api/categorias

Request
JSON
{
  "nombre": "Exfoliacion"
}
Response
JSON
{
  "id": 6,
  "nombre": "Exfoliacion"
}

201 Created: Recurso creado con éxito.

3. Actualizar categoría
Método: PUT
URL:/api/categorias/{id}


Request
JSON
{
  "nombre": "Cuidado facial"
}
Response
JSON
{
  "id": 2,
  "nombre": "Cuidado facial"
}
200 OK: Petición exitosa.

4. Eliminar categoría
Método: DELETE
URL:/api/categorias/{id}

204 No Content : No retorna contenido

📋 Códigos de Estado HTTP
200 OK: Petición exitosa.

201 Created: Recurso creado con éxito.

204 No Content : No retorna contenido

400 Bad Request: Datos de entrada inválidos.

404 Not Found: El producto no existe.

500 Internal Server Error: Error en el servidor cloud o base de datos.

⚠️ Formato de Errores
En caso de error, la API responderá con el siguiente formato:

JSON
{
  "timestamp": "2026-03-24T21:58:00",
  "status": 404,
  "error": "Not Found",
  "message": "Mensaje detallado del error",
  "path": "/api/productos/id"
}
