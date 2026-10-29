# Verificación del backend

Fecha: 29 de octubre de 2026. Entorno local de desarrollo.

## Compilación

- JDK Microsoft 21.0.12.1 y Maven Wrapper 3.10.0.
- Spring Boot 4.1.2 y Springdoc 3.2.0, conservados del proyecto inicial.
- Ejecutable: `target/AutoAndes-0.0.1-SNAPSHOT.jar`.
- Compilación y empaquetado correctos.

## Integración con base temporal

Comando: `./mvnw.cmd test`.

Resultado: **14 pruebas, 0 fallos, 0 errores, 0 omitidas**. Base H2 temporal, separada de Oracle.

Se verificaron firma y expiración JWT, cuenta desactivada después del login, mensaje uniforme de credenciales, unión de permisos, 401/403, validaciones por campo, 404, JSON inválido, precios y usuario determinados por servidor, restricciones de eliminación, servicio inactivo en tercer detalle, repetidos, cantidades, kilometraje, anulación según día y motivo, reporte, filtros y preflight CORS.

Una prueba agrega temporalmente una restricción en H2 que rechaza la segunda línea después de insertar la cabecera y la primera línea. Se comprueba que la transacción revierte ambas tablas. Esta restricción solo existe durante la prueba y no se crea en Oracle.

Reporte de Maven: `target/surefire-reports/pe.edu.upeu.autoandes.BackendIntegrationTest.txt`.

## Ejecución real contra Oracle

Oracle respondió en `localhost:1522/FREEPDB1` con el esquema `AutoAndesDB`. El esquema estaba vacío antes de iniciar. Flyway aplicó V1 correctamente y Hibernate validó las entidades. Un segundo arranque reconoció la migración existente sin repetir la semilla.

Comando: `postman collection run postman/AutoAndes.postman_collection.json --no-report-events`.

Resultado con Postman CLI 1.74.0: **50 solicitudes, 96 aserciones, 0 fallos**, código de salida 0. No se publicaron resultados en un workspace de Postman. La colección exportada queda disponible para repetir las comprobaciones.

Se comprobaron los tres usuarios activos y el inactivo, CRUD, duplicados, cliente inexistente, validaciones, permisos de asesor y jefe, rechazo de servicio inactivo, ausencia de una orden parcial después del rechazo, cálculo del total pese a un total adulterado, filtros, anulación y exclusión de anuladas del reporte. La edición y la anulación inválidas también devuelven `campos`.

## Auditoría SQL posterior

| Comprobación en Oracle | Resultado |
|---|---|
| Clientes | 6 |
| Servicios | 8 |
| Vehículos | 8 |
| Usuarios con hash BCrypt | 4 |
| Órdenes REGISTRADAS | 3 |
| Órdenes ANULADAS | 3 |
| Cabeceras cuyo total no coincide con sus detalles | 0 |
| Detalles cuyo subtotal no coincide con cantidad × precio | 0 |
| Detalles huérfanos | 0 |

Las dos ejecuciones de la colección dejaron dos órdenes de evidencia ANULADAS, adicionales a la ANULADA de la semilla. Los servicios y vehículos auxiliares de CRUD se eliminaron al finalizar las pruebas. Los saltos de numeración por solicitudes rechazadas son normales en una secuencia Oracle.

## Swagger y contrato

- Swagger UI: HTTP 200 en `http://localhost:8080/swagger-ui/index.html`.
- OpenAPI: 12 rutas y 19 operaciones; autenticación Bearer definida y login público.
- Se comprueban las respuestas documentadas 201 de altas y 204 de eliminaciones.
- Exportación local: `docs/openapi.json`, generada desde el backend en ejecución. Después de cambiar los controladores, regenerarla desde `/v3/api-docs`.

El contrato explicado para uso de la SPA está en `docs/contrato-api.md`; las instrucciones de arranque y usuarios están en `README.md`.

## Verificación posterior: estructura de Pharma

Después de convertir los DTO a clases, integrar CrudService y centralizar los enums se repitieron las pruebas: 14 pruebas de integración y 50 solicitudes Postman con 96 aserciones aprobadas. Se comparó OpenAPI antes y después y resultó idéntico. La evidencia de esta etapa y sus archivos está en `docs/avances.md`; los conteos SQL anteriores corresponden a la auditoría inicial.

## Integración con el frontend

Se repitió Maven: 14 pruebas aprobadas. La SPA pasó 14 pruebas unitarias y cinco recorridos de Chrome contra la API y Oracle reales. La colección Postman pasó de nuevo sus 50 solicitudes y 96 aserciones en un verificador temporal en 8081 que utiliza los YAML actuales y el archivo local de credenciales. Se comprobó que la nueva clave privada del equipo está activa y que no aparece en los archivos versionados. Las evidencias de interfaz están en el repositorio del frontend, `docs/verificacion.md`.
