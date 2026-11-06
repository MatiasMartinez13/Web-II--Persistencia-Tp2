package com.example.tp1.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Usuario registrado")
public record UsuarioResponseDTO(
        @Schema(example = "1") Long id,
        @Schema(example = "persona@example.com") String email,
        @Schema(example = "USER") String rol
) {}
