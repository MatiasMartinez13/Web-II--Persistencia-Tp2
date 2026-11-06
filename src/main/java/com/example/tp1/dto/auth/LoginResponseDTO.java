package com.example.tp1.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de login con token JWT")
public record LoginResponseDTO(
        @Schema(example = "eyJhbGciOiJIUzI1NiJ9...") String token,
        @Schema(example = "Bearer") String tokenType
) {}
