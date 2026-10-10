# Evidencia del TP2

Respuestas **reales** obtenidas ejecutando la aplicación contra un **PostgreSQL 16** corriendo
localmente (binarios EDB en `localhost:5432`, base `apirest`), Spring Boot 4.1.1 / Java 25,
`target/tp1-0.0.1-SNAPSHOT.jar` en `http://localhost:8080`.

Cada archivo guarda la respuesta JSON **exacta** que devolvió la API en el momento de la prueba,
incluyendo el código de estado indicado en su nombre. Las respuestas de favoritos ya incluyen
`listaId` (persistencia en PostgreSQL).

## 1. Swagger UI / OpenAPI

| Archivo | Contenido |
| --- | --- |
| `swagger/openapi.json` | OpenAPI 3.1 generado en `/v3/api-docs` con los **tres grupos** (Productos, Favoritos y Listas), incluida la operación `mover-favoritos`. |

Swagger UI navegable (con la app corriendo): http://localhost:8080/swagger-ui.html

Para la entrega podés capturar la página de Swagger UI con `Win+Shift+S` mostrando
los tres grupos **Productos**, **Favoritos** y **Listas**.

## 2. Catálogo de productos — caso de éxito y de error (sin cambios vs TP1)

| Archivo | Prueba | Resultado |
| --- | --- | --- |
| `productos/exito-lista.json` | `GET /api/productos?limit=2&skip=0` | **200** — página con `contenido`, `total`, `skip`, `limit`, `totalPaginas` |
| `productos/exito-uno.json` | `GET /api/productos/1` | **200** — producto mapeado al contrato propio |
| `productos/error-404.json` | `GET /api/productos/99999` | **404** — `ApiError` uniforme |
| `productos/error-400.json` | `GET /api/productos?limit=500` | **400** — validación de parámetro (`limit ≤ 100`) |

## 3. Favoritos — persistidos en PostgreSQL (ahora con `listaId`)

| Archivo | Prueba | Resultado |
| --- | --- | --- |
| `favoritos/exito-crear-201.json` | `POST /api/favoritos` | **201** — favorito creado con `id`, `fechaAgregado` y `listaId` |
| `favoritos/exito-listar.json` | `GET /api/favoritos` | **200** — todos los favoritos de la base |
| `favoritos/exito-uno.json` | `GET /api/favoritos/{id}` | **200** — favorito por id |
| `favoritos/exito-actualizar-200.json` | `PUT /api/favoritos/{id}` | **200** — favorito actualizado |
| `favoritos/exito-listar-tras-reinicio.json` | `GET /api/favoritos` (tras reiniciar la app) | **200** — **los favoritos sobreviven al reinicio** (persistencia real) |
| `favoritos/error-400.json` | `POST /api/favoritos` (nota "") | **400** — validación con detalle por campo |
| `favoritos/error-400-sin-lista.json` | `POST /api/favoritos` sin `listaId` | **400** — `fieldErrors.listaId` |
| `favoritos/error-404.json` | `GET /api/favoritos/9999` | **404** — favorito inexistente |
| `favoritos/error-404-lista.json` | `POST /api/favoritos` con lista inexistente | **404** — `No existe una lista con el id 9999` |

## 4. Listas

| Archivo | Prueba | Resultado |
| --- | --- | --- |
| `listas/exito-crear-201.json` | `POST /api/listas` | **201** — lista creada |
| `listas/exito-listar.json` | `GET /api/listas` | **200** — todas las listas |
| `listas/exito-uno.json` | `GET /api/listas/{id}` | **200** — lista por id |
| `listas/exito-favoritos-de-lista.json` | `GET /api/listas/{id}/favoritos` | **200** — favoritos de esa lista |
| `listas/exito-eliminar-204.json` | `DELETE /api/listas/{id}` (vacía) | **204** — sin contenido |
| `listas/error-400.json` | `POST /api/listas` (nombre "  ") | **400** — validación |
| `listas/error-404.json` | `GET /api/listas/9999` | **404** |
| `listas/error-409-eliminar-con-favoritos.json` | `DELETE /api/listas/{id}` con favoritos | **409 Conflict** — no se puede borrar una lista con favoritos |
| `listas/exito-listar-tras-reinicio.json` | `GET /api/listas` (tras reiniciar) | **200** — solo quedan la lista destino y la creada por V4 |

## 5. Operación transaccional `mover-favoritos`

| Archivo | Prueba | Resultado |
| --- | --- | --- |
| `listas/exito-mover-favoritos.json` | `POST /api/listas/{origen}/mover-favoritos` `{ "destinoId": destino }` | **200** — devuelve la lista destino |
| `listas/exito-favoritos-tras-mover.json` | `GET /api/listas/{destino}/favoritos` | **200** — los 2 favoritos quedaron en la lista destino |
| `listas/error-404-origen-borrada.json` | `GET /api/listas/{origen}` | **404** — la lista origen fue eliminada |
| `listas/error-400-mover.json` | mover con `destinoId == origen` | **400** |
| `listas/error-404-mover.json` | mover a lista inexistente | **404** |

## 6. Migraciones (Flyway) y evolución del esquema

| Archivo | Contenido |
| --- | --- |
| `migraciones/flyway-history.txt` | Historial de `flyway_schema_history` en la base nueva: **V1..V4** con `success = true` |
| `migraciones/flyway-log.txt` | Log del arranque: migraciones aplicadas y validación de Hibernate OK |
| `migraciones/esquema.txt` | Tablas públicas y columnas de `favoritos` (incl. `lista_id NOT NULL`) |
| `infra/apirest-startup.log` | Log de arranque de la app sobre la base nueva (Flyway + Tomcat) durante la verificación |
| `migraciones/v4-datos-previos-antes.txt` | **Escenario de datos previos**: favorito legado **sin lista** en el esquema V1 |
| `migraciones/flyway-log-v4.txt` | Arranque con `baseline-on-migrate`: Flyway aplica V2, V3 y **V4** sobre el esquema heredado |
| `migraciones/flyway-history-v4.txt` | Historial tras la adopción: V1 (baseline) + V2, V3, V4 aplicadas |
| `migraciones/v4-datos-previos-despues.txt` | El favorito legado fue reasignado a **"Sin clasificar"** y `lista_id` quedó **NOT NULL** |
| `migraciones/apirest-operativa-tras-v4.json` | `GET /api/favoritos` con la app corriendo sobre la base migrada → **200** |

## 7. Colección de pruebas manuales

El archivo **`apirest.http`** (raíz del proyecto) contiene un caso por endpoint — incluidos los
recursos de listas, los errores 400/404/409 y `mover-favoritos` — importable en la extensión
**REST Client** de VS Code.

## Cómo reproducir las pruebas

```bash
# 1) Levantar PostgreSQL
docker compose up -d

# 2) Levantar la app
.\mvnw.cmd spring-boot:run

# 3) Repetir una prueba puntual, por ejemplo eliminar una lista con favoritos -> 409
curl -i -X DELETE "http://localhost:8080/api/listas/1"
```