# Contrato AutoAndes API v1

Fuente: secciones 3 y 4 del examen AutoAndes. Prefijo `/api/v1`.
OpenAPI generado desde los DTO y controladores: `/v3/api-docs`; interfaz `/swagger-ui.html`.

| Método | Ruta | Permiso | Éxito |
|---|---|---|---|
| POST | /auth/login | Público | 200 |
| GET | /auth/me | Autenticado | 200 |
| GET | /clientes | VEHICULO_LEER | 200 |
| GET | /servicios y /servicios/{id} | SERVICIO_LEER | 200 |
| POST | /servicios | SERVICIO_ESCRIBIR | 201 |
| PUT | /servicios/{id} | SERVICIO_ESCRIBIR | 200 |
| DELETE | /servicios/{id} | SERVICIO_ESCRIBIR | 204 |
| PATCH | /servicios/{id}/estado | SERVICIO_ESCRIBIR | 200 |
| GET | /vehiculos?clienteId y /vehiculos/{id} | VEHICULO_LEER | 200 |
| POST | /vehiculos | VEHICULO_ESCRIBIR | 201 |
| PUT | /vehiculos/{id} | VEHICULO_ESCRIBIR | 200 |
| DELETE | /vehiculos/{id} | VEHICULO_ESCRIBIR | 204 |
| POST | /ordenes | ORDEN_REGISTRAR | 201 |
| GET | /ordenes?placa&estado&desde&hasta y /ordenes/{id} | ORDEN_LEER | 200 |
| PATCH | /ordenes/{id}/anular | ORDEN_ANULAR | 200 |
| GET | /reportes/ingresos-por-servicio?desde&hasta | REPORTE_VER | 200 |

Todas las operaciones de negocio requieren Bearer JWT. Solo POST de login, Swagger y OPTIONS son públicos.

Ejemplos de solicitudes:

```json
{
  "login": {"username":"asesor","password":"Auto2026*"},
  "servicio": {"codigo":"SRV-009","nombre":"Diagnóstico de motor","precio":80.00,"duracionMin":45,"activo":true},
  "estado": {"activo":false},
  "vehiculo": {"placa":"ABC-123","marca":"Toyota","modelo":"Yaris","anio":2024,"clienteId":1},
  "orden": {"vehiculoId":1,"kilometraje":46000,"detalles":[{"servicioId":1,"cantidad":1}]},
  "anulacion": {"motivo":"Registro duplicado por el asesor"}
}
```

Respuestas: Cliente devuelve id, documento, nombres, telefono. Servicio devuelve id y campos de solicitud. Vehículo devuelve id y campos de solicitud, además de clienteNombre. Orden devuelve id, numero, fecha, vehiculoId, placa, clienteNombre, kilometraje, estado, total, usuarioRegistro, motivoAnulacion y detalles (id, servicioId, codigo, servicio, cantidad, precioUnitario, subtotal). Reporte devuelve codigo, servicio, cantidadTotal e importeTotal.

Login devuelve token, tipo Bearer, expiraEnSegundos (3600), username, nombreCompleto, roles y permisos. `/auth/me` devuelve id, username, nombreCompleto, activo, roles y permisos. Nunca se devuelve password.

Filtros opcionales y combinables; fechas ISO yyyy-MM-dd con ambos días incluidos; órdenes por fecha e id descendentes. Placa por coincidencia parcial sin distinguir mayúsculas. Sin paginación; sin resultados devuelve `[]`.

Errores 400/401/403/404/409/500: timestamp, status, error, mensaje, ruta. Solo errores de validación incluyen campos. Ejemplo:

```json
{"timestamp":"2026-10-29T09:14:22","status":400,"error":"Bad Request","mensaje":"Datos inválidos","ruta":"/api/v1/vehiculos","campos":{"placa":"La placa debe tener el formato ABC-123"}}
```

JSON malformado, datos inválidos, rango invertido, enum inválido, límites y servicios repetidos: 400. Token ausente, alterado o vencido: 401. Credenciales erróneas, usuario inexistente e inactivo reciben el mismo mensaje 401. Permiso insuficiente: 403. Recurso inexistente: 404. Duplicados, recursos utilizados, kilometraje decreciente, servicio inactivo y anulación fuera de día/estado: 409.

Precio, subtotal y total se calculan en backend. Fecha y usuario se toman del servidor y la autenticación. La numeración usa secuencia Oracle y admite saltos por rollback. La semilla ocupa OS-000001 a OS-000004. Día de negocio: America/Lima.
