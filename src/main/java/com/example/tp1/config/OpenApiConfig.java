package com.example.tp1.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "TP2 - Persistent and transactional",
                version = "2.0.0",
                description = "API REST que expone un catálogo de productos (consumido de la API pública " +
                        "DummyJSON) y un CRUD de favoritos y listas persistidos en PostgreSQL. " +
                        "El esquema se versiona con Flyway y las entidades se acceden con Spring Data JPA. " +
                        "Arquitectura en capas con puertos y adaptadores (persistencia intercambiable).",
                contact = @Contact(name = "TP2")
        ),
        tags = {
                @Tag(name = "Productos", description = "Catálogo de productos de solo lectura (DummyJSON)."),
                @Tag(name = "Favoritos", description = "CRUD de favoritos persistidos en PostgreSQL."),
                @Tag(name = "Listas", description = "CRUD de listas y operación transaccional mover-favoritos.")
        }
)
public class OpenApiConfig {
}