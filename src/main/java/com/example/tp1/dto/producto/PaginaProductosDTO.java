package com.example.tp1.dto.producto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Página de resultados de productos con su propio contrato de paginación")
public record PaginaProductosDTO(

        @Schema(description = "Productos de la página actual")
        List<ProductoDTO> contenido,

        @Schema(description = "Cantidad total de productos disponibles", example = "194")
        int total,

        @Schema(description = "Cantidad de productos a saltar", example = "0")
        int skip,

        @Schema(description = "Cantidad de productos por página", example = "30")
        int limit,

        @Schema(description = "Cantidad total de páginas", example = "7")
        int totalPaginas
) {
}