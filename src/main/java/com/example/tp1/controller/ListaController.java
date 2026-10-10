package com.example.tp1.controller;

import com.example.tp1.dto.favorito.FavoritoResponseDTO;
import com.example.tp1.dto.lista.ListaRequestDTO;
import com.example.tp1.dto.lista.ListaResponseDTO;
import com.example.tp1.dto.lista.MoverFavoritosRequestDTO;
import com.example.tp1.service.ListaService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/listas")
@Tag(name = "Listas", description = "CRUD de listas persistentes en PostgreSQL y operación transaccional mover-favoritos.")
public class ListaController {

    private final ListaService service;

    public ListaController(ListaService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
            summary = "Crea una lista",
            description = "Crea una nueva lista en PostgreSQL y la devuelve con estado 201 Created."
    )
    public ResponseEntity<ListaResponseDTO> crear(@Valid @RequestBody ListaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @GetMapping
    @Operation(summary = "Lista las listas", description = "Devuelve todas las listas persistidas.")
    public List<ListaResponseDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtiene una lista por su id",
            description = "Devuelve una lista existente o 404 si no existe."
    )
    public ListaResponseDTO obtener(
            @Parameter(description = "Id de la lista", example = "1")
            @PathVariable
            @Positive(message = "El id debe ser un número positivo")
            long id) {
        return service.obtener(id);
    }

    @GetMapping("/{id}/favoritos")
    @Operation(
            summary = "Lista los favoritos de una lista",
            description = "Devuelve los favoritos que pertenecen a la lista indicada, o 404 si la lista no existe."
    )
    public List<FavoritoResponseDTO> listarFavoritos(
            @Parameter(description = "Id de la lista", example = "1")
            @PathVariable
            @Positive(message = "El id debe ser un número positivo")
            long id) {
        return service.listarFavoritos(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Elimina una lista",
            description = "Elimina una lista vacía. Responde 204 No Content si se eliminó, " +
                    "404 si no existe y 409 Conflict si aún tiene favoritos vinculados."
    )
    public void eliminar(
            @Parameter(description = "Id de la lista", example = "1")
            @PathVariable
            @Positive(message = "El id debe ser un número positivo")
            long id) {
        service.eliminar(id);
    }

    @PostMapping("/{id}/mover-favoritos")
    @Operation(
            summary = "Mueve todos los favoritos de una lista a otra",
            description = "Operación transaccional: reasigna todos los favoritos de la lista origen a la lista destino " +
                    "y elimina la lista origen. Se revierte todo si falla cualquier paso. " +
                    "404 si alguna lista no existe, 400 si origen y destino son la misma."
    )
    public ListaResponseDTO moverFavoritos(
            @Parameter(description = "Id de la lista origen", example = "1")
            @PathVariable("id")
            @Positive(message = "El id debe ser un número positivo")
            long id,
            @Valid @RequestBody MoverFavoritosRequestDTO dto) {
        return service.moverFavoritos(id, dto.destinoId());
    }
}