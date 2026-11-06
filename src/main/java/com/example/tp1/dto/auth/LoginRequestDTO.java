package com.example.tp1.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Datos de inicio de sesión")
public record LoginRequestDTO(
        @Schema(example = "persona@example.com")
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        String email,

        @Schema(example = "secreta123")
        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {}
