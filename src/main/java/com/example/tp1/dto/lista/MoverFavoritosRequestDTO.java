package com.example.tp1.dto.lista;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Cuerpo de la operación mover-favoritos: lista destino")
public record MoverFavoritosRequestDTO(

        @Schema(description = "Id de la lista destino a la que se mueven los favoritos", example = "2")
        @NotNull(message = "El destinoId es obligatorio")
        @Positive(message = "El destinoId debe ser un número positivo")
        Long destinoId
) {
}