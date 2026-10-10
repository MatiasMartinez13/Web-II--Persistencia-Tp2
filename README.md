# TP2 · Persistent and transactional

API REST con **Spring Boot 4.1 / Java 25 / Maven / PostgreSQL**, organizada en capas con
**puertos y adaptadores** (Controller → Service → Port ← Persistence Adapter).

La persistencia deja de ser en memoria: **favoritos y listas viven en PostgreSQL**, el esquema
se versiona con **Flyway** y las entidades se acceden con **Spring Data JPA**.

Expone tres grupos de endpoints:

- **`/api/productos`** — catálogo de solo lectura. El backend consume la API pública
  [DummyJSON](https://dummyjson.com) con `RestClient` (sin cambios respecto del TP1).
- **`/api/favoritos`** — CRUD completo de favoritos, ahora **persistidos en PostgreSQL**.
- **`/api/listas`** — CRUD de listas + operación transaccional **`mover-favoritos`**.

## Requisitos

- **JDK 25** (verificar con `java -version`).
- **PostgreSQL 14+** (puerto `5432`) — ver abajo cómo levantarlo.
- No hace falta Maven instalado: el proyecto incluye el **Maven Wrapper** (`mvnw`/`mvnw.cmd`).

> **Importante (variables de entorno):** si tu `JAVA_HOME` apunta a un JDK distinto del 25
> (por ejemplo JDK 21), el wrapper puede fallar con `release version 25 not supported`.
> Solución: seteá `JAVA_HOME` al JDK 25 o dejalo sin definir para que se use el `java` del `PATH`.
> En Visual Studio Code, `.vscode/settings.json` ya registra el runtime **JavaSE-25**.

## Cómo levantar PostgreSQL

### Opción A — Docker (recomendada)

```bash
docker compose up -d
```

Crea el contenedor `apirest-postgres` con la base `apirest`, usuario/contraseña
`postgres`/`postgres` en el puerto **5432** (datos persistentes en el volumen `postgres_data`).

### Opción B — PostgreSQL instalado en la máquina

1. Instalá PostgreSQL (por ejemplo los binarios oficiales de [enterprisedb.com](https://www.enterprisedb.com/download-postgresql-binaries)).
2. Creá la base con el mismo nombre y credenciales:

```bash
# con psql como superusuario
CREATE DATABASE apirest;
ALTER USER postgres WITH PASSWORD 'postgres';
```

> Las credenciales/configuración están en `src/main/resources/application.properties`
> (`spring.datasource.*`). Si tu instalación usa otra base/usuario, ajustalos ahí.

## Cómo levantar el proyecto

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

En Visual Studio Code: ejecutá la clase `com.example.tp1.Tp1Application` (botón ▶).

La aplicación queda escuchando en **http://localhost:8080**. Al arrancar, **Flyway aplica
automáticamente las migraciones pendientes** sobre la base configurada.

> A diferencia del TP1, **los datos persisten tras reiniciar la aplicación** (PostgreSQL).

## Migraciones (Flyway)

El esquema se versiona en `src/main/resources/db/migration`:

| Migración | Qué hace |
| --- | --- |
| `V1__create_favoritos.sql` | Tabla `favoritos` (id, producto_id, nota, fecha_alta) |
| `V2__create_listas.sql` | Tabla `listas` (id, nombre) |
| `V3__add_lista_id_a_favoritos.sql` | Columna `lista_id` (nullable) + FK `fk_favoritos_lista` → `listas(id)` |
| `V4__lista_id_obligatorio.sql` | Crea la lista por defecto **"Sin clasificar"**, reasigna los favoritos sin lista y declara `lista_id NOT NULL` |

**¿Por qué no dejar que Hibernate cree el esquema con `ddl-auto=update`?**

Con Flyway **una sola herramienta** es dueña del esquema. `spring.jpa.hibernate.ddl-auto=validate`
hace que Hibernate **valide** que las entidades coincidan con el esquema versionado y **no lo modifique**.
Así la evolución queda registrada, auditable y reproducible en cualquier ambiente.

**¿Por qué la `V4` es una migración y no un `ALTER` improvisado?**

Porque hay que **asegurar la consistencia de los datos previos**: si existieran favoritos sin
lista (datos de un esquema anterior), fallaría cualquier `ALTER ... SET NOT NULL`. La migración
los **reasigna a la lista por defecto** antes de endurecer la restricción. Una migración Flyway
tiene el beneficio adicional de poder **revertirse/saltarse** según la versión de la base y queda
registrada en `flyway_schema_history`.

## Documentación (Swagger / OpenAPI)

Con la app corriendo:

| Recurso | URL |
| --- | --- |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

## Endpoints

### Catálogo de productos (`/api/productos`)

| Método | Ruta | Descripción | Código de éxito |
| --- | --- | --- | --- |
| `GET` | `/api/productos?limit=30&skip=0` | Lista paginada de productos | 200 |
| `GET` | `/api/productos/{id}` | Producto por id (404 si no existe) | 200 |

### Favoritos (`/api/favoritos`)

| Operación | Método y ruta | Código de éxito |
| --- | --- | --- |
| Crear | `POST /api/favoritos` | **201 Created** |
| Listar | `GET /api/favoritos` | 200 OK |
| Obtener uno | `GET /api/favoritos/{id}` | 200 OK |
| Actualizar (reemplazo) | `PUT /api/favoritos/{id}` | 200 OK |
| Eliminar | `DELETE /api/favoritos/{id}` | **204 No Content** |

DTO de entrada (ahora con `listaId`):

```json
{
  "productoId": 1,
  "nota": "Ideal para regalar",
  "listaId": 1
}
```

DTO de salida:

```json
{
  "id": 1,
  "productoId": 1,
  "nota": "Ideal para regalar",
  "fechaAgregado": "2026-09-17T10:30:00",
  "listaId": 1
}
```

### Listas (`/api/listas`)

| Operación | Método y ruta | Código de éxito |
| --- | --- | --- |
| Crear | `POST /api/listas` | **201 Created** |
| Listar | `GET /api/listas` | 200 OK |
| Obtener una | `GET /api/listas/{id}` | 200 OK |
| Listar sus favoritos | `GET /api/listas/{id}/favoritos` | 200 OK |
| Eliminar (vacía) | `DELETE /api/listas/{id}` | **204 No Content** |
| Mover favoritos | `POST /api/listas/{origenId}/mover-favoritos` | 200 OK |

`DELETE /api/listas/{id}` devuelve **409 Conflict** si la lista aún tiene favoritos
(no se puede eliminar borrando el historial). Para vaciarla primero: mover sus favoritos
a otra lista o eliminarlos uno a uno.

`POST /api/listas/{origenId}/mover-favoritos` recibe el id de la lista destino:

```json
{
  "destinoId": 2
}
```

Mueve **todos** los favoritos de la lista origen a la lista destino y **elimina la lista origen**
en una misma transacción. Devuelve la lista destino. **400** si origen == destino, **404** si
alguna lista no existe.

### Relación entre entidades

- Una **lista** tiene muchos **favoritos**.
- Un **favorito** pertenece a exactamente **una** lista (`favoritos.lista_id` NOT NULL → FK
  `fk_favoritos_lista`). En el modelo se modela con `@ManyToOne` desde `FavoritoEntity` hacia
  `ListaEntity`, y la API lo expone como `listaId` en cada favorito y como recurso anidado
  `GET /api/listas/{id}/favoritos`.

## Operación transaccional: `mover-favoritos`

El método `ListaService.moverFavoritos(...)` está anotado con **`@Transactional`** y ejecuta,
en una única transacción de base de datos, cuatro operaciones:

1. asigna la lista destino a cada favorito de la lista origen,
2. confirma cada reasignación (`favoritoRepository.save`),
3. elimina la lista origen.

**¿Por qué es atómica (ACID)?** Si el proceso fallara a mitad de camino (error en un `save` o
caída) sin transacción, quedaría un estado inconsistente: favoritos movidos "a medias" que
referencian una lista que ya no existe, o una lista origen vacía que nunca se borró. Con
`@Transactional`, **o se aplican todas las operaciones, o ninguna se persiste**: Spring abre la
transacción antes del primer acceso, y si se lanza una excepción **hace rollback** de todo lo
perseguido. Eso garantiza, en el sentido ACID, **atomicidad** (todo o nada), **consistencia**
(los favoritos siempre apuntan a listas existentes) y **aislamiento** (otras operaciones ven el
estado final, no un intermedio).

`moverFavoritos` valida además que las dos listas existan (404) y que no sean la misma (400)
**antes** de modificar nada.

## Validación

DTO / parámetros con **Bean Validation**:

- `ListaRequestDTO` — `nombre`: `@NotBlank` + `@Size(max = 100)`.
- `FavoritoRequestDTO` — `productoId`: `@NotNull` + `@Positive`; `nota`: `@NotBlank` + `@Size(max = 500)`;
  `listaId`: `@NotNull` + `@Positive`.
- `MoverFavoritosRequestDTO` — `destinoId`: `@NotNull` + `@Positive`.
- Ids de ruta con `@Positive` (controllers con `@Validated`).

Ante un error de validación la API responde **400 Bad Request** con el detalle por campo (ver
`evidencia/` y `apirest.http`).

## Manejo uniforme de errores

`GlobalExceptionHandler` (`@RestControllerAdvice`) responde siempre con `ApiError`
(timestamp, status, error, message, path y, si corresponde, `fieldErrors`):

| Caso | Código |
| --- | --- |
| Favorito, producto o **lista** inexistente | **404 Not Found** |
| **Eliminar lista con favoritos** | **409 Conflict** |
| Violación de integridad de la base (ej. FK) | 409 Conflict |
| Validación fallida (body o parámetros) | **400 Bad Request** |
| Cuerpo malformado / tipo de id inválido / argumento inválido | **400 Bad Request** |
| Falla al consumir DummyJSON | **502 Bad Gateway** |
| Recurso no existente | 404 |
| Error inesperado | 500 |

## Arquitectura: puertos y adaptadores

```
                ┌──────────────┐     ┌─────────────────────────────────┐
  Controller ──▶│   Service    │────▶│ Port (interfaz de persistencia) │
                │ (lógica de   │     └──────────────────┬──────────────┘
                │  dominio)    │                        │
                └──────▲───────┘  ┌─────────────────────▼──────────────┐
                       │          │ Adapter JPA (PostgreSQL actual)    │
                       └──────────┤ FavoritoRepositoryAdapter          │
                                  └────────────────────────────────────┘
```

- **Port** (`repository/FavoritoRepository`, `repository/ListaRepository`): contrato de
  persistencia definido por el dominio, sin ninguna dependencia de infraestructura.
- **Adapter** (`persistence/adapter/*`): implementa el port con **Spring Data JPA**. El servicio
  **no conoce** JPA, entidades, Hibernate ni SQL; solo conoce el port. Por eso en TP1 el mismo
  dominio funcionaba con `InMemoryFavoritoRepository` y ahora con PostgreSQL sin tocar la lógica
  de negocio: la persistencia es **intercambiable** (esa es la diferencia puerto vs. adaptador).

```
src/main/java/com/example/tp1/
├── Tp1Application.java
├── client/            DummyJsonClient (RestClient) + DTOs crudos de DummyJSON
├── config/            Config de RestClient (timeouts) y OpenAPI
├── controller/        ProductoController, FavoritoController y ListaController
├── domain/            Entidades de dominio Favorito y Lista (sin dependencias de infra)
├── dto/               DTOs desacoplados del modelo interno
├── exception/         ApiError, excepciones propias y GlobalExceptionHandler
├── mapper/            Mapeo entidad ⇄ DTO (a mano)
├── repository/        Puertos: FavoritoRepository y ListaRepository
├── persistence/       Persistencia (adapter externo):
│   ├── adapter/       FavoritoRepositoryAdapter y ListaRepositoryAdapter
│   ├── entity/        Entidades JPA (FavoritoEntity, ListaEntity)
│   └── repository/    JpaRepositories (Spring Data)
└── service/           FavoritoService, ListaService y ProductoService
```

## Pruebas

### Automáticas

```bash
.\mvnw.cmd test
```

- `FavoritoServiceTest` / `ListaServiceTest`: **unitarios** de los services usando **fakes**
  que implementan los puertos (sin base de datos ni Mockito).
- `FavoritoApiTest` / `ListaApiTest`: **integración** contra un **PostgreSQL real** levantado con
  **Testcontainers** (MockMvc + contenedor `postgres:16-alpine`). Cubren CRUD, validaciones
  400/404/409 y `mover-favoritos`.
  Se **omiten automáticamente** si Docker no está disponible (`@Testcontainers(disabledWithoutDocker = true)`);
  los tests unitarios corren siempre.

> Para correr la suite completa (incluida la integración) necesitás Docker activo.

### Manuales — `apirest.http`

Colección lista para usar con la extensión **REST Client** de VS Code, con un caso por endpoint,
incluidos los de error (400/404/409) y la operación `mover-favoritos`.

## Evidencia

- Carpeta **`evidencia/`** con las respuestas **reales** obtenidas contra un **PostgreSQL 16**
  corriendo en local: catálogo, favoritos (con `listaId`), listas (éxito/error 400/404/409),
  `mover-favoritos`, swagger, e **`migraciones/`** con el historial de Flyway y la prueba del
  escenario de datos previos (adopción del esquema heredado). Detalle en `evidencia/README.md`.
- `apirest.http`: colección de pruebas manuales.
- Swagger UI: http://localhost:8080/swagger-ui.html — muestra los tres grupos.

## Puntos de la consigna y dónde está cada uno

1. **PostgreSQL + Spring Data JPA**: `persistence/` (entidades JPA, JpaRepositories y adapters)
   + `application.properties` (`spring.datasource.*`).
2. **Reemplazo del repo en memoria por un adapter JPA sin tocar dominio/Service/Controller**:
   `FavoritoRepositoryAdapter` implementa el port `FavoritoRepository`; `InMemoryFavoritoRepository`
   fue eliminado y **ningún Service dependía de él** (dependían del port).
3. **Flyway + evolución del esquema**: `db/migration/V1..V4` (ver tabla de migraciones arriba).
4. **Modelar relación y exponerla en la API**: `@ManyToOne` en `FavoritoEntity`; la API la expone
   como `listaId` en los favoritos y como `GET /api/listas/{id}/favoritos`.
5. **Operación transaccional con justificación ACID**: `ListaService.moverFavoritos` con
   `@Transactional` (sección "Operación transaccional").
6. **Puerto vs. adaptador**: sección "Arquitectura: puertos y adaptadores".