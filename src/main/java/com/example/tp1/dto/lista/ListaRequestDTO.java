package com.example.tp1.dto.lista;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos de entrada para crear una lista")
public record ListaRequestDTO(

        @Schema(description = "Nombre de la lista", example = "Regalos")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre
) {
}