# Evidencia TP3 — Seguridad e integración continua

## Qué se agregó

| Entrega | Dónde | Estado |
| --- | --- | --- |
| Auth JWT (registro/login) | `service/UsuarioService.java`, `controller/AuthController.java` | ✅ implementado |
| Autorización dueño/ADMIN | `service/ListaService.java`, `service/FavoritoService.java` (`verificarAcceso` → 403) | ✅ implementado |
| Migraciones V5/V6 | `db/migration/V5__create_usuarios.sql`, `V6__add_usuario_id_a_listas.sql` | ✅ aplicadas en tests con base limpia |
| Tests unitarios + IT | `src/test` — `*Test` (Surefire) + `*IT` (Failsafe/Testcontainers) | ✅ `mvn test` verde (25 tests) |
| CI | `.github/workflows/ci.yml` (`./mvnw verify`) | ✅ workflow creado; pendiente primer push |
| Swagger `bearerAuth` + tags Auth/Salud | `config/OpenApiConfig.java` | ✅ implementado |
| `GET /health` | `controller/HealthController.java` | ✅ implementado |
| Despliegue Render | `Dockerfile` + variables de entorno (ver README) | ⚠️ pasos documentados, URL pendiente |

## Cómo verificar local

```bash
docker compose up -d
.\mvnw.cmd verify     # incluye V5/V6 contra PostgreSQL limpio + suite IT
```

Cubierto por tests: login 200/401, lista propia 200, ajena 403, sin token 401, ADMIN 200.

## Despliegue

Ver **README.md § Despliegue (Render)**. Completar tras el deploy:

- URL pública: `https://<servicio>.onrender.com` — **pendiente**
- Evidencia `GET /health` → `{"status":"UP"}` — **pendiente (cubierto por AuthControllerIT)**
- Endpoint protegido sin token → 401 / con token → 200 — **pendiente (cubierto por tests)**
- Link al workflow de GitHub Actions — **pendiente al pushear a GitHub**
