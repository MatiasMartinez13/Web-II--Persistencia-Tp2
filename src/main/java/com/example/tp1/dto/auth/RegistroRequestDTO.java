package com.example.tp1.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos de registro de un nuevo usuario")
public record RegistroRequestDTO(
        @Schema(description = "Email del usuario (se usa como identidad)", example = "persona@example.com")
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        String email,

        @Schema(description = "Contraseña (mínimo 8 caracteres)", example = "secreta123")
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password
) {}
