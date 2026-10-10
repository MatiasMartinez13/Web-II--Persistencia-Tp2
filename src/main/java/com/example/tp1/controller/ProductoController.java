package com.example.tp1.controller;

import com.example.tp1.dto.producto.PaginaProductosDTO;
import com.example.tp1.dto.producto.ProductoDTO;
import com.example.tp1.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Catálogo de productos de solo lectura. El backend consume la API pública DummyJSON y expone su propio contrato.")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(
            summary = "Lista productos del catálogo",
            description = "Devuelve una página de productos con su propio contrato de paginación, " +
                    "reutilizando y validando los parámetros limit/skip de la API externa DummyJSON."
    )
    public PaginaProductosDTO listar(
            @Parameter(description = "Cantidad de productos a devolver")
            @RequestParam(defaultValue = "30")
            @Min(value = 1, message = "limit debe ser mayor o igual a 1")
            @Max(value = 100, message = "limit debe ser menor o igual a 100")
            int limit,

            @Parameter(description = "Cantidad de productos a omitir (paginar)")
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "skip debe ser mayor o igual a 0")
            int skip) {
        return service.listar(limit, skip);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtiene un producto por su id",
            description = "Busca el producto en la API externa y devuelve su representación propia. " +
                    "404 si el producto no existe."
    )
    public ProductoDTO obtener(
            @Parameter(description = "Id del producto", example = "1")
            @PathVariable
            @Positive(message = "El id debe ser un número positivo")
            long id) {
        return service.obtener(id);
    }
}