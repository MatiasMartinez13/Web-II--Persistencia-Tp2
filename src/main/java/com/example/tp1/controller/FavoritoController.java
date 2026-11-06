package com.example.tp1.controller;

import com.example.tp1.dto.favorito.FavoritoRequestDTO;
import com.example.tp1.dto.favorito.FavoritoResponseDTO;
import com.example.tp1.service.FavoritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/favoritos")
@Tag(name = "Favoritos", description = "CRUD completo de favoritos persistidos en PostgreSQL. Requiere autenticación Bearer; cada usuario solo opera sobre favoritos de sus listas (ADMIN ve todos).")
public class FavoritoController {

    private final FavoritoService service;

    public FavoritoController(FavoritoService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
            summary = "Crea un favorito",
            description = "Guarda un nuevo favorito en memoria y lo devuelve con estado 201 Created."
    )
    public ResponseEntity<FavoritoResponseDTO> crear(@Valid @RequestBody FavoritoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @GetMapping
    @Operation(summary = "Lista los favoritos", description = "Devuelve todos los favoritos guardados en memoria.")
    public List<FavoritoResponseDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtiene un favorito por su id",
            description = "Devuelve un favorito existente o 404 si no existe."
    )
    public FavoritoResponseDTO obtener(
            @Parameter(description = "Id del favorito", example = "1")
            @PathVariable
            @Positive(message = "El id debe ser un número positivo")
            long id) {
        return service.obtener(id);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualiza un favorito",
            description = "Reemplaza los datos de un favorito existente o devuelve 404 si no existe."
    )
    public FavoritoResponseDTO actualizar(
            @Parameter(description = "Id del favorito", example = "1")
            @PathVariable
            @Positive(message = "El id debe ser un número positivo")
            long id,
            @Valid @RequestBody FavoritoRequestDTO dto) {
        return service.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Elimina un favorito",
            description = "Elimina un favorito existente. Responde 204 No Content o 404 si no existe."
    )
    public void eliminar(
            @Parameter(description = "Id del favorito", example = "1")
            @PathVariable
            @Positive(message = "El id debe ser un número positivo")
            long id) {
        service.eliminar(id);
    }
}