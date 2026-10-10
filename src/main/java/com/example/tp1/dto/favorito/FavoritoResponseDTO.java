package com.example.tp1.dto.favorito;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Favorito tal como lo devuelve la API")
public record FavoritoResponseDTO(

        @Schema(description = "Identificador del favorito", example = "1")
        Long id,

        @Schema(description = "Id del producto externo", example = "1")
        Long productoId,

        @Schema(description = "Nota personal del usuario", example = "Ideal para regalar")
        String nota,

        @Schema(description = "Fecha y hora en la que se agregó el favorito", example = "2026-09-17T10:30:00")
        LocalDateTime fechaAgregado,

        @Schema(description = "Id de la lista a la que pertenece el favorito", example = "1")
        Long listaId
) {
}