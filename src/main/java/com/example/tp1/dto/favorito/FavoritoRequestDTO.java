package com.example.tp1.dto.favorito;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos de entrada para crear o actualizar un favorito")
public record FavoritoRequestDTO(

        @Schema(description = "Id del producto externo que se quiere guardar como favorito", example = "1")
        @NotNull(message = "El productoId es obligatorio")
        @Positive(message = "El productoId debe ser un número positivo")
        Long productoId,

        @Schema(description = "Nota personal del usuario sobre el favorito", example = "Ideal para regalar")
        @NotBlank(message = "La nota es obligatoria")
        @Size(max = 500, message = "La nota no puede superar los 500 caracteres")
        String nota,

        @Schema(description = "Id de la lista a la que pertenece el favorito", example = "1")
        @NotNull(message = "El listaId es obligatorio")
        @Positive(message = "El listaId debe ser un número positivo")
        Long listaId
) {
}