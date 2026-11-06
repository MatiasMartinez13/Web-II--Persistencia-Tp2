package com.example.tp1.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "TP3 - Seguridad, pruebas e integración continua",
                version = "3.0.0",
                description = "API REST autenticada con JWT. /api/productos es pública; /api/favoritos y /api/listas requieren Authorization: Bearer <token> (ver /api/auth/registro y /api/auth/login). " +
                        "Cada usuario solo opera sobre sus listas; ADMIN ve todas. Esquema con Flyway y JPA.",
                contact = @Contact(name = "TP3")
        ),
        tags = {
                @Tag(name = "Productos", description = "Catálogo público de solo lectura (DummyJSON). Sin autenticación."),
                @Tag(name = "Favoritos", description = "CRUD de favoritos. Requiere autenticación (Bearer). Solo favoritos del usuario dueño (ADMIN ve todos)."),
                @Tag(name = "Listas", description = "CRUD de listas y mover-favoritos. Requiere autenticación (Bearer). Solo listas del usuario dueño (ADMIN ve todas)."),
                @Tag(name = "Autenticación", description = "Registro y login (público)."),
                @Tag(name = "Salud", description = "Estado del servicio (público).")
        },
        security = {
                @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}