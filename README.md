# AutoAndes Backend

API REST del taller mecánico AutoAndes, basada en la organización por capas y la seguridad JWT del proyecto de referencia del curso. Implementa servicios, vehículos, órdenes cabecera-detalle, anulación, reporte agregado y permisos.

Repositorios: [backend](https://github.com/branpachman-lang/autoandes-backend) y [frontend](https://github.com/branpachman-lang/autoandes-frontend). El frontend se ejecuta en `http://localhost:4200`.

## Requisitos y ejecución

- JDK 21; Maven Wrapper incluido.
- Oracle disponible en `localhost:1522/FREEPDB1` y esquema `AutoAndesDB`, con permisos de creación de tablas, índices y secuencias.
- El perfil `dev` usa la conexión configurada en `application-dev.yaml`. No es necesario ejecutar el SQL manualmente: Flyway aplica la migración V1 y luego Hibernate valida las entidades.

Desde PowerShell, dentro de esta carpeta:

Al clonar, copiar `application-local.example.yaml` como `application-local.yaml` y completar la contraseña Oracle y una clave JWT Base64 de al menos 32 bytes aleatorios. El archivo local está excluido de Git. También se pueden proporcionar `DB_PASSWORD` y `JWT_SECRET` mediante variables de entorno. En el equipo de desarrollo ya está preparado el archivo local.

```powershell
# Solo si JAVA_HOME no está configurado; esta es la instalación encontrada en el equipo.
$env:JAVA_HOME = "$env:USERPROFILE\.jdks\ms-21.0.12.1"
.\mvnw.cmd spring-boot:run
```

Swagger: http://localhost:8080/swagger-ui.html

OpenAPI: http://localhost:8080/v3/api-docs

API: http://localhost:8080/api/v1

Para usar otro puerto sin cambiar los YAML:

```powershell
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.arguments=--server.port=8081'
```

La migración está en `src/main/resources/db/migration/V1__esquema_y_semilla.sql`. Requiere un esquema inicialmente vacío y se ejecuta una sola vez. Los cambios futuros de esquema deben añadirse en nuevas migraciones; no editar una migración que ya se aplicó.

## Usuarios de desarrollo

| Usuario | Contraseña de prueba | Rol | Estado |
|---|---|---|---|
| admin | Auto2026* | ADMIN | Activo |
| asesor | Auto2026* | ASESOR | Activo |
| jefe | Auto2026* | JEFE_TALLER | Activo |
| inactivo | Auto2026* | ASESOR | Inactivo |

Las contraseñas se insertan como hash BCrypt. El login nunca devuelve el hash. La semilla también incluye seis clientes, ocho servicios (SRV-008 inactivo), ocho vehículos, tres órdenes REGISTRADAS y una ANULADA. AAA-111 tiene una orden REGISTRADA de 45000 km.

En Swagger, ejecutar POST `/api/v1/auth/login`, copiar `token` y pegarlo en **Authorize**, sin anteponer Bearer. Luego probar `/auth/me` y los recursos. El token dura 3600 segundos.

## Contrato y permisos

El contrato, los cuerpos JSON, las respuestas y los filtros se describen en [docs/contrato-api.md](docs/contrato-api.md). Todas las listas provienen de Oracle y las entidades se transforman a DTO.

El contrato OpenAPI exportado está en `docs/openapi.json`. Los resultados de compilación, pruebas y auditoría SQL están en [docs/verificacion.md](docs/verificacion.md).

## Estructura siguiendo Pharma

Los DTO son clases con `@AllArgsConstructor`, `@NoArgsConstructor`, `@Getter` y `@Setter`. Cada solicitud y respuesta mantiene su clase independiente y las validaciones permanecen en los Request DTO.

```text
dto/                 solicitudes y respuestas de negocio
  auth/              LoginRequestDTO, LoginResponseDTO, UsuarioResponseDTO
  reporte/           IngresoPorServicioDTO
exception/dto/       ErrorResponseDTO
enums/               EstadoOrden, Permiso
service/
  generic/           CrudService<REQ, RES, ID>
  service/           interfaces de cada servicio
  impl/              implementaciones y reglas de negocio
```

`ServicioService` y `VehiculoService` extienden `CrudService`, con los métodos `create`, `update`, `read`, `delete` y `readAll`, como en Pharma. Sus implementaciones conservan las validaciones, transacciones y restricciones de cada recurso. Vehículos añade `listarPorCliente` y servicios añade `cambiarEstado`.

Cliente mantiene una interfaz de consulta; órdenes conserva sus operaciones de registro, consulta y anulación. Los enums se importan desde `enums`, incluso en las consultas JPQL.

La evolución y los archivos que sirven como evidencia están en [docs/avances.md](docs/avances.md).

| Permiso | ADMIN | ASESOR | JEFE_TALLER |
|---|---|---|---|
| SERVICIO_LEER | Sí | Sí | Sí |
| SERVICIO_ESCRIBIR | Sí | No | Sí |
| VEHICULO_LEER | Sí | Sí | Sí |
| VEHICULO_ESCRIBIR | Sí | Sí | No |
| ORDEN_LEER | Sí | Sí | Sí |
| ORDEN_REGISTRAR | Sí | Sí | No |
| ORDEN_ANULAR | Sí | No | Sí |
| REPORTE_VER | Sí | No | Sí |

Los permisos se centralizan en `security/PermisosPorRol.java` y se verifican con authorities en `config/SecurityConfig.java`. El reporte además usa `@PreAuthorize`. El filtro comprueba firma, expiración y estado actual del usuario. Sin autenticación válida responde 401; con autenticación sin permiso responde 403.

## Recorrido de una orden

1. `controller/OrdenController` recibe y valida `OrdenRequestDTO`.
2. Spring Security exige ORDEN_REGISTRAR y proporciona el usuario autenticado.
3. `service/impl/OrdenServiceImpl.registrar` abre una transacción, bloquea el vehículo, valida kilometraje y servicios, toma precios de la base y calcula con BigDecimal.
4. `OrdenRepository` guarda cabecera y detalles mediante cascade. Un fallo revierte todo.
5. `mapper/AutoAndesMapper` produce la respuesta completa con el número de orden.

`entity/OrdenServicio` contiene la relación cabecera-detalle; `repository/DetalleOrdenRepository` contiene el reporte JPQL con GROUP BY. El precio unitario del detalle queda como precio histórico, incluso si cambia el catálogo.

La anulación exige el mismo día en America/Lima y motivo de 10 a 200 caracteres. Los filtros desde/hasta incluyen ambos días. La secuencia de números puede tener saltos por rechazos o rollback, pero no genera duplicados.

## Pruebas

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

Las pruebas de integración usan una base H2 temporal y no modifican Oracle. Cubren JWT, cuenta inactiva, unión de permisos, 401/403, validaciones, cálculo en servidor, restricciones de eliminación, rollback, kilometraje, anulación, reporte, filtros y CORS. Además se comprueba rollback ante un fallo real de persistencia.

Importar `postman/AutoAndes.postman_collection.json` en Postman, o ejecutar con Postman CLI mientras el backend está iniciado:

```powershell
postman collection run postman/AutoAndes.postman_collection.json --no-report-events
```

Para otro puerto:

```powershell
postman collection run postman/AutoAndes.postman_collection.json --env-var baseUrl=http://localhost:8081/api/v1 --no-report-events
```

La colección contiene 50 solicitudes ordenadas y aserciones de contrato. Crea y elimina recursos auxiliares de CRUD; registra una orden de evidencia y la anula, conservando ese registro para inspección. Los tokens se obtienen durante la ejecución y no están guardados en el archivo.

## Configuración

Se conservaron los perfiles de conexión preparados en el proyecto. Desarrollo importa opcionalmente `application-local.yaml`, que contiene las credenciales del equipo y está excluido de Git. `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y `JWT_SECRET` también permiten configurar la conexión y la clave sin editar los archivos versionados. Producción exige esas variables; activar con `SPRING_PROFILES_ACTIVE=prod`.

En ambos perfiles Flyway administra el esquema y `ddl-auto: validate` evita que Hibernate lo modifique por su cuenta. CORS permite `http://localhost:4200`, los métodos del contrato y Authorization. El paquete raíz es `pe.edu.upeu.autoandes`.
