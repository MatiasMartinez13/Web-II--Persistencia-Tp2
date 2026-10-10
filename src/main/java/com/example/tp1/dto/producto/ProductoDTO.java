package com.example.tp1.dto.producto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Producto del catálogo, en el contrato propio de la API")
public record ProductoDTO(

        @Schema(description = "Identificador del producto", example = "1")
        long id,

        @Schema(description = "Título del producto", example = "iPhone 9")
        String titulo,

        @Schema(description = "Descripción del producto", example = "An apple mobile which is nothing like apple")
        String descripcion,

        @Schema(description = "Precio en dólares", example = "549.0")
        double precio,

        @Schema(description = "Categoría del producto", example = "smartphones")
        String categoria,

        @Schema(description = "Puntaje promedio de reseñas (1 a 5)", example = "4.69")
        double rating,

        @Schema(description = "Stock disponible", example = "94")
        int stock,

        @Schema(description = "URL de la imagen principal", example = "https://cdn.dummyjson.com/products/...")
        String imagenUrl
) {
}