# Evidencia de avances de AutoAndes

Fecha: 29 de octubre de 2026.

## Avance 1: backend funcional

Se integraron Oracle, Flyway, CRUD de servicios y vehículos, consulta de clientes, órdenes cabecera-detalle, anulación, reporte agregado y seguridad JWT basada en el proyecto de referencia. Las evidencias iniciales están en `docs/verificacion.md` y la colección `postman/AutoAndes.postman_collection.json`.

## Avance 2: estructura de Pharma aplicada

### DTO como clases

Se convirtieron los 16 DTO a clases Java con constructores vacío y completo, getters y setters de Lombok. Se actualizaron sus consumidores a `getCodigo()`, `getClienteId()`, `getDetalles()` y demás getters. Las anotaciones de validación siguen en los campos y se conserva la validación de los detalles anidados.

Se organizaron los DTO por su responsabilidad:

| Ubicación | Evidencia |
|---|---|
| `dto/` | Clases Request/Response de servicios, vehículos y órdenes; respuesta de clientes y solicitudes de estado/anulación |
| `dto/auth/` | LoginRequestDTO, LoginResponseDTO y UsuarioResponseDTO |
| `dto/reporte/` | IngresoPorServicioDTO, con constructor utilizado por la proyección JPQL |
| `exception/dto/` | ErrorResponseDTO, conservando mensaje, ruta y campos |

### Interfaz genérica

Se incorporó `service/generic/CrudService.java`, con los mismos parámetros genéricos y operaciones que Pharma:

```java
public interface CrudService<REQ, RES, ID> {
    RES create(REQ request);
    RES update(ID id, REQ request);
    RES read(ID id);
    void delete(ID id);
    Iterable<RES> readAll();
}
```

Las interfaces específicas están en `service/service/`. `ServicioService` y `VehiculoService` heredan del contrato genérico; sus implementaciones están en `service/impl/` y marcan las operaciones de interfaz con `@Override`.

Recorrido para demostrarlo: `ServicioController` → `ServicioService extends CrudService` → `ServicioServiceImpl.create` → repositorio → mapper → `ServicioResponseDTO`. El mismo recorrido se aplica a vehículos. Cliente, órdenes, autenticación y reportes conservan interfaces acordes con sus operaciones.

### Enums

- `enums/EstadoOrden.java`: REGISTRADA y ANULADA. `OrdenServicio.estado` usa este tipo y lo guarda con `@Enumerated(EnumType.STRING)`.
- `enums/Permiso.java`: los ocho permisos del caso. `security/PermisosPorRol` concede esos permisos y `SecurityConfig` los exige por operación.
- Se actualizaron imports, pruebas y la referencia al enum y al DTO del reporte dentro de JPQL.

### Verificación de este avance

- `mvnw.cmd clean package`: compilación y empaquetado correctos con Java 21; 14 pruebas de integración aprobadas, sin fallos ni errores.
- Colección existente de Postman contra Oracle: 50 solicitudes y 96 aserciones aprobadas, código de salida 0.
- Comparación del OpenAPI anterior con el generado después de reorganizar: documentos idénticos, incluidas las 12 rutas, 19 operaciones, respuestas y esquemas DTO.
- Oracle reconoció la migración existente y el backend reinició correctamente en el puerto 8080.
- La ejecución de la colección conserva una orden de evidencia anulada, según lo descrito en el README.

Para demostrar el avance, abrir `dto/ServicioRequestDTO.java`, `service/generic/CrudService.java`, `service/service/ServicioService.java`, `service/impl/ServicioServiceImpl.java`, `enums/EstadoOrden.java` y `enums/Permiso.java`; después ejecutar las mismas solicitudes de Swagger o Postman.

## Avance 3: integración con la SPA y repositorios

Se construyó AutoAndesFrontend con la arquitectura y el estilo de Pharma. Las pruebas integradas comprobaron CRUD, órdenes, kilometraje, anulación, reportes, permisos y sesión desde Chrome contra la API real. El frontend tiene 14 pruebas unitarias y cinco recorridos de navegador aprobados; el backend conserva 14 pruebas de integración aprobadas y su colección de 50 solicitudes con 96 aserciones también pasó contra Oracle.

Se separaron las credenciales del equipo en `application-local.yaml`, excluido de Git. El perfil de desarrollo importa ese archivo y el repositorio incluye `application-local.example.yaml`. La clave JWT local se generó nuevamente y no coincide con la clave de las pruebas unitarias. Se comprobó la configuración real en un verificador temporal en 8081.

El historial usa `main`, `develop` y `examen-a-pacho`, con commits por etapa. El frontend incluye en `docs/verificacion.md` las capturas de los recorridos y las instrucciones para reproducirlos.
