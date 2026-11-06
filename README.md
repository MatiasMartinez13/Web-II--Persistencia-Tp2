# TP3 · Seguridad, pruebas e integración continua

API REST con **Spring Boot 4.1 / Java 25 / Maven / PostgreSQL**, organizada en capas con
**puertos y adaptadores** (Controller → Service → Port ← Persistence Adapter).

Evolución del TP2: se suma **autenticación con JWT y autorización por dueño/rol**.
**Favoritos y listas siguen siendo lo mismo**; ahora una lista pertenece a un usuario
(`Usuario → Lista → Favorito`). El catálogo `/api/productos` sigue público.

| Grupo | Acceso |
| --- | --- |
| `/api/auth/**`, `/api/productos/**`, `/health` | Públicos |
| `/api/favoritos/**`, `/api/listas/**` | Requieren `Authorization: Bearer <token>` |
| `ADMIN` | Ve/modifica listas y favoritos de cualquiera |
| `USER` | Solo las suyas; lista ajena → **403 Forbidden** |

## Requisitos

- **JDK 25** (`java -version`). Maven Wrapper incluido (`mvnw`/`mvnw.cmd`).
- **Docker** para los tests de integración (Testcontainers + `postgres:16-alpine`).
- **PostgreSQL 14+** local solo si querés correr la app suelta (`docker compose up -d`).

> Si `JAVA_HOME` apunta a otro JDK (ej. 21), el wrapper puede fallar con
> `release version 25 not supported`: seteá `JAVA_HOME` al JDK 25 o dejalo sin definir.

## Cómo levantar el proyecto

```bash
docker compose up -d          # PostgreSQL local apirest (postgres/postgres)

.\mvnw.cmd spring-boot:run    # Windows
./mvnw spring-boot:run        # Linux/macOS
```

La app escucha en **http://localhost:8080** y Flyway aplica las migraciones al arrancar.
Swagger: http://localhost:8080/swagger-ui.html (botón **Authorize** → `Bearer <token>`).

Configuración por **variables de entorno** (defaults solo para local):

| Variable | Default local | Uso |
| --- | --- | --- |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/apirest` | JDBC de PostgreSQL |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | `postgres` / `postgres` | Credenciales |
| `JWT_SECRET` | clave de desarrollo | Secreto para firmar JWT (**cambiar en producción**) |
| `JWT_EXPIRATION` | `3600000` (1 h, ms) | Vencimiento del token |
| `PORT` | `8080` | Puerto (Render/otros PaaS lo inyectan) |

## Endpoints

### Autenticación (`/api/auth`)

| Método | Ruta | Qué hace |
| --- | --- | --- |
| `POST` | `/api/auth/registro` | Registra usuario rol `USER`, contraseña con BCrypt. `201`, `409` si email existe |
| `POST` | `/api/auth/login` | Valida credenciales y devuelve `{token, tokenType: Bearer}`. `401` si falla |

```json
// POST /api/auth/registro
{ "email": "persona@example.com", "password": "secreta123" }
```

```json
// POST /api/auth/login -> 200
{ "token": "eyJ...", "tokenType": "Bearer" }
```

Luego: `Authorization: Bearer <token>` en cada request a listas/favoritos.

### Catálogo (`/api/productos`, público)

| Método | Ruta | Éxito |
| --- | --- | --- |
| `GET` | `/api/productos?limit=30&skip=0` | 200 |
| `GET` | `/api/productos/{id}` | 200 / 404 |

### Listas (`/api/listas`, Bearer)

| Operación | Método y ruta | Éxito |
| --- | --- | --- |
| Crear (dueño = usuario autenticado) | `POST /api/listas` | 201 |
| Listar (solo propias; todas si `ADMIN`) | `GET /api/listas` | 200 |
| Obtener / ver favoritos / eliminar | `GET`/`GET`/`DELETE /api/listas/{id}`(`/favoritos`) | 200/204, **403 si ajena**, 404 |
| Mover favoritos | `POST /api/listas/{id}/mover-favoritos` | 200, 403 si origen/destino ajenas |

### Favoritos (`/api/favoritos`, Bearer)

CRUD completo (`POST` 201, `GET`, `GET /{id}`, `PUT`, `DELETE` 204). Solo favoritos de listas
propias (`ADMIN`: todos). Crear/editar contra lista ajena → 403.

### Salud

`GET /health` → `200 {"status":"UP"}` (público; para verificar despliegues).

## Migraciones (Flyway)

| Migración | Qué hace |
| --- | --- |
| `V1__create_favoritos.sql` | `favoritos` |
| `V2__create_listas.sql` | `listas` |
| `V3__add_lista_id_a_favoritos.sql` | FK favoritas→listas |
| `V4__lista_id_obligatorio.sql` | Lista "Sin clasificar" + NOT NULL |
| `V5__create_usuarios.sql` | `usuarios` (id, email único, password_hash, rol) |
| `V6__add_usuario_id_a_listas.sql` | `listas.usuario_id` NOT NULL + FK → usuarios; listas heredadas reasignadas a usuario semilla `semilla@local` antes de endurecer |

## ¿Dónde vive la autorización y por qué? (Consigna 4.5)

**En el Service (`ListaService` / `FavoritoService`), no en el Controller ni en anotaciones.**

```java
CurrentUser actual = securityHelper.requireUser();
if (!actual.esAdmin() && !actual.id().equals(lista.getUsuarioId())) {
    throw new AccessDeniedException("No tenés acceso a la lista ..."); // -> 403
}
```

Elegí el Service porque la regla es **lógica de dominio** ("solo tu dueño puede tocar esta
lista"), no formato HTTP:

1. **Mismo criterio en todo punto de entrada.** `obtener`, `eliminar`, `listarFavoritos` y
   `moverFavoritos` comparten `obtenerEntidad()` que ya verifica acceso; un `GET` o un
   `DELETE` no pueden olvidarla. En el Controller habría que repetirla en cada método y
   además mezcla transporte con negocio.
2. **Reutilizable fuera de HTTP.** Si mañana hay otro adaptador (CLI, tests sin MockMvc)
   no pasa por el Controller; en cambio todo lo que llame al Service queda protegido.
3. **Distingue 403 vs. 404 deliberadamente.** `obtenerEntidad()` primero busca (404 si no
   existe) y luego verifica dueño antes de devolver (403 si existe pero es ajena), tal como
   pide la consigna. Esa decisión no es un detalle de Spring Security: es una regla de
   negocio, y vive donde vive el negocio.
4. **Fácil de testear.** Los unitarios (`ListaServiceImplTest`) autenticación simulada con
   `TestSecurity` y verifican `obtener_listaAjena_lanzaAccesoDenegado` sin subir Spring.

El **Service solo conoce el `CurrentUser`** (puesto en el `SecurityContext` por
`JwtAuthenticationFilter`); no conoce HTTP, JWT ni cabeceras. El Controller sigue fino.

## Pruebas

```bash
.\mvnw.cmd test        # Surefire: *Test (unitarios, sin Docker)
.\mvnw.cmd verify      # Surefire + Failsafe: *IT con Testcontainers (base limpia + Flyway)
```

- **Unitarios**: `ListaServiceImplTest`, `UsuarioServiceTest`, `FavoritoServiceTest`
  (fakes en memoria, método `operación_escenario_resultado`).
- **Integración (`*IT`, Failsafe)**: `AuthControllerIT`, `ListaControllerIT`,
  `FavoritoControllerIT` — PostgreSQL real por Testcontainers + migraciones Flyway en cada
  corrida. Cubren: login 200/401, registro 201/409, lista propia 200, ajena 403, sin token 401,
  ADMIN sobre ajena 200, y el CRUD anterior de listas/favoritos autenticado.
- Si Docker no está disponible, los `*IT` se **omiten** (`disabledWithoutDocker`), el build no
  falla; los unitarios siempre corren.

El sufijo manda: `*Test` → Surefire (`mvn test`), `*IT` → Failsafe (`mvn verify`).

## Integración continua (GitHub Actions)

Workflow **`.github/workflows/ci.yml`**: en cada `push`/`pull_request` corre en Ubuntu con
JDK 25:

```bash
chmod +x mvnw
./mvnw test && ./mvnw verify -Dsurefire.useFile=false  # ver nota abajo
```

En la práctica el workflow ejecuta `./mvnw verify`, que incluye `test` de Surefire **y**
los `*IT` de Failsafe (con Testcontainers, sin PostgreSQL local). Para chequear el resultado:
pestaña **Actions** del repo → corrida `CI` → job **build** → **Tests run** en el log.
Si un test falla, el job queda **rojo**; corregí y volvé a pushear.

> Si el paso de Maven falla con `Permission denied` en el wrapper, corré
> `git update-index --chmod=+x mvnw` y subí el cambio.

## Despliegue (Render, ejemplo)

1. Creá **PostgreSQL** en Render (plan free) y copiá `DATABASE_URL`/`DATABASE_USERNAME`/`DATABASE_PASSWORD`.
2. Creá un **Web Service** (Docker) apuntando a este repo; `Dockerfile` incluido.
3. Environment Variables:

   ```
   DATABASE_URL=jdbc:postgresql://<host>:5432/<db>
   DATABASE_USERNAME=<usuario>
   DATABASE_PASSWORD=<clave>
   JWT_SECRET=<secreto largo y aleatorio>   # ¡nunca el de desarrollo!
   PORT=10000                                # o ignorar si la plataforma inyecta PORT
   ```

4. Deploy. Al arrancar, Flyway migra la base remota. Verificá:

   ```
   GET https://<tu-servicio>.onrender.com/health        -> {"status":"UP"}
   GET .../api/listas (sin token)                       -> 401
   POST .../api/auth/login + Authorization: Bearer ...   -> 200
   ```

   Revisá los **Logs** del servicio: debe verse `Started Tp1Application` sin errores de
   Flyway/conexión. Podés suspender el servicio tras verificar; alcanza la evidencia.

## Evidencia / puntos de consigna

- `V5`/`V6` aplicadas vía Flyway, sin ediciones posteriores.
- Suite verde local y en CI (ver Actions).
- `GET /health` y 401/200 sobre endpoint protegido.
- Swagger con auth (`Auth` tag + `bearerAuth` scheme) y listas/favoritos marcados "requiere autenticación".
