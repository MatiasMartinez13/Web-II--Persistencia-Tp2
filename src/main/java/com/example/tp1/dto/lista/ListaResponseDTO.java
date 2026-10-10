package com.example.tp1.dto.lista;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lista tal como la devuelve la API")
public record ListaResponseDTO(

        @Schema(description = "Identificador de la lista", example = "1")
        Long id,

        @Schema(description = "Nombre de la lista", example = "Regalos")
        String nombre
) {
}