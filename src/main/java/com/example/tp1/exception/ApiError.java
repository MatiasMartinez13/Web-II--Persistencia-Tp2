package com.example.tp1.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Estructura uniforme de respuesta ante errores")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(

        @Schema(description = "Marca de tiempo en que ocurrió el error", example = "2026-09-17T10:30:00Z")
        Instant timestamp,

        @Schema(description = "Código de estado HTTP", example = "404")
        int status,

        @Schema(description = "Frase estándar del estado HTTP", example = "Not Found")
        String error,

        @Schema(description = "Descripción legible del error", example = "No existe un favorito con el id 99")
        String message,

        @Schema(description = "Ruta del endpoint que produjo el error", example = "/api/favoritos/99")
        String path,

        @Schema(description = "Detalle de errores por campo (solo en validación fallida)")
        Map<String, String> fieldErrors
) {

    public ApiError(HttpStatus status, String message, String path, Map<String, String> fieldErrors) {
        this(Instant.now(), status.value(), status.getReasonPhrase(), message, path, fieldErrors);
    }
}