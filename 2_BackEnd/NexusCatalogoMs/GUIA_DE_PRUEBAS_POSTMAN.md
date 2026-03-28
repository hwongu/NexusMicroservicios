# GUIA DE PRUEBAS POSTMAN

## Microservicio
`NexusCatalogoMs`

## URL base
`http://localhost:8082/NexusMS`

## Categorias

### GET /api/categorias
- Metodo HTTP
  `GET`
- URL completa
  `http://localhost:8082/NexusMS/api/categorias`
- Body
  No aplica
- Respuesta esperada
```json
[
  {
    "idCategoria": 1,
    "nombre": "Monitores",
    "descripcion": "Pantallas y accesorios"
  }
]
```
- Codigos HTTP posibles
  `200 OK`
  `500 Internal Server Error`

### GET /api/categorias/{id}
- Metodo HTTP
  `GET`
- URL completa
  `http://localhost:8082/NexusMS/api/categorias/1`
- Body
  No aplica
- Respuesta esperada
```json
{
  "idCategoria": 1,
  "nombre": "Monitores",
  "descripcion": "Pantallas y accesorios"
}
```
- Codigos HTTP posibles
  `200 OK`
  `404 Not Found`
  `500 Internal Server Error`
- Observaciones
  Si el id no existe, el mensaje de error es `Categoria no encontrada.`

### POST /api/categorias
- Metodo HTTP
  `POST`
- URL completa
  `http://localhost:8082/NexusMS/api/categorias`
- Body de ejemplo
```json
{
  "nombre": "Monitores",
  "descripcion": "Pantallas y accesorios"
}
```
- Respuesta esperada
```json
{
  "idCategoria": 1,
  "nombre": "Monitores",
  "descripcion": "Pantallas y accesorios"
}
```
- Codigos HTTP posibles
  `201 Created`
  `400 Bad Request`
  `500 Internal Server Error`
- Observaciones
  `nombre` es obligatorio.

### PUT /api/categorias/{id}
- Metodo HTTP
  `PUT`
- URL completa
  `http://localhost:8082/NexusMS/api/categorias/1`
- Body de ejemplo
```json
{
  "nombre": "Monitores Gamer",
  "descripcion": "Pantallas, soportes y accesorios"
}
```
- Respuesta esperada
```json
{
  "message": "Categoria actualizada exitosamente"
}
```
- Codigos HTTP posibles
  `200 OK`
  `400 Bad Request`
  `404 Not Found`
  `500 Internal Server Error`

### DELETE /api/categorias/{id}
- Metodo HTTP
  `DELETE`
- URL completa
  `http://localhost:8082/NexusMS/api/categorias/1`
- Body
  No aplica
- Respuesta esperada
  Sin contenido
- Codigos HTTP posibles
  `204 No Content`
  `404 Not Found`
  `409 Conflict`
  `500 Internal Server Error`
- Observaciones
  Si la categoria tiene productos asociados, el mensaje de error es `No se puede eliminar la categoria porque tiene productos asociados.`

## Productos

### GET /api/productos
- Metodo HTTP
  `GET`
- URL completa
  `http://localhost:8082/NexusMS/api/productos`
- Body
  No aplica
- Respuesta esperada
```json
[
  {
    "idProducto": 1,
    "idCategoria": 1,
    "nombreCategoria": "Laptops",
    "nombre": "Laptop Lenovo ThinkPad T14",
    "precio": 1200.5,
    "stock": 50
  }
]
```
- Codigos HTTP posibles
  `200 OK`
  `500 Internal Server Error`

### GET /api/productos/{id}
- Metodo HTTP
  `GET`
- URL completa
  `http://localhost:8082/NexusMS/api/productos/1`
- Body
  No aplica
- Respuesta esperada
```json
{
  "idProducto": 1,
  "idCategoria": 1,
  "nombreCategoria": "Laptops",
  "nombre": "Laptop Lenovo ThinkPad T14",
  "precio": 1200.5,
  "stock": 50
}
```
- Codigos HTTP posibles
  `200 OK`
  `404 Not Found`
  `500 Internal Server Error`
- Observaciones
  Si el id no existe, el mensaje de error es `Producto no encontrado.`

### POST /api/productos
- Metodo HTTP
  `POST`
- URL completa
  `http://localhost:8082/NexusMS/api/productos`
- Body de ejemplo
```json
{
  "idCategoria": 1,
  "nombre": "Laptop Lenovo ThinkPad T14",
  "precio": 1200.5,
  "stock": 50
}
```
- Respuesta esperada
```json
{
  "idProducto": 1,
  "idCategoria": 1,
  "nombreCategoria": "Laptops",
  "nombre": "Laptop Lenovo ThinkPad T14",
  "precio": 1200.5,
  "stock": 50
}
```
- Codigos HTTP posibles
  `201 Created`
  `400 Bad Request`
  `500 Internal Server Error`
- Observaciones
  `idCategoria`, `nombre`, `precio` y `stock` son obligatorios. `precio` debe ser mayor a 0. `stock` no puede ser negativo. Si la categoria no existe, el mensaje de error es `La categoria indicada no existe.`

### PUT /api/productos/{id}
- Metodo HTTP
  `PUT`
- URL completa
  `http://localhost:8082/NexusMS/api/productos/1`
- Body de ejemplo
```json
{
  "idCategoria": 1,
  "nombre": "Laptop Lenovo ThinkPad T14 Gen 2",
  "precio": 1350.0,
  "stock": 45
}
```
- Respuesta esperada
```json
{
  "message": "Producto actualizado exitosamente"
}
```
- Codigos HTTP posibles
  `200 OK`
  `400 Bad Request`
  `404 Not Found`
  `500 Internal Server Error`
- Observaciones
  Si el producto no existe, el mensaje de error es `Producto no encontrado.` Si la categoria no existe, el mensaje de error es `La categoria indicada no existe.`

### PUT /api/productos/{id}/stock
- Metodo HTTP
  `PUT`
- URL completa
  `http://localhost:8082/NexusMS/api/productos/1/stock`
- Body de ejemplo para sumar
```json
{
  "cantidad": 10,
  "operacion": "SUMAR"
}
```
- Body de ejemplo para restar
```json
{
  "cantidad": 10,
  "operacion": "RESTAR"
}
```
- Respuesta esperada
```json
{
  "message": "Stock del producto actualizado exitosamente"
}
```
- Codigos HTTP posibles
  `200 OK`
  `400 Bad Request`
  `404 Not Found`
  `409 Conflict`
  `500 Internal Server Error`
- Observaciones
  `cantidad` es obligatoria y debe ser mayor a cero. `operacion` es obligatoria y debe ser `SUMAR` o `RESTAR`. Si el producto no existe, el mensaje de error es `Producto no encontrado.` Si se intenta restar mas stock del disponible, el mensaje de error es `No se puede restar el stock porque el producto quedaria con stock negativo.`

### DELETE /api/productos/{id}
- Metodo HTTP
  `DELETE`
- URL completa
  `http://localhost:8082/NexusMS/api/productos/1`
- Body
  No aplica
- Respuesta esperada
  Sin contenido
- Codigos HTTP posibles
  `204 No Content`
  `404 Not Found`
  `409 Conflict`
  `500 Internal Server Error`
- Observaciones
  Si el producto esta referenciado en otra tabla, el mensaje de error es `No se puede eliminar el producto porque esta referenciado en un ingreso.`

## Formato basico de error

Cuando ocurre un error controlado, la API responde con esta estructura:

```json
{
  "timestamp": "2026-03-26T18:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": {
    "nombre": "El nombre del producto es obligatorio."
  },
  "path": "/NexusMS/api/productos"
}
```

Si el error no es de validacion, `message` llega como texto.
