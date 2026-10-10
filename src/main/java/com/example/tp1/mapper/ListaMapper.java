package com.example.tp1.mapper;

import com.example.tp1.domain.Lista;
import com.example.tp1.dto.lista.ListaRequestDTO;
import com.example.tp1.dto.lista.ListaResponseDTO;

public final class ListaMapper {

    private ListaMapper() {
    }

    public static Lista toEntity(ListaRequestDTO dto) {
        Lista lista = new Lista();
        lista.setNombre(dto.nombre());
        return lista;
    }

    public static ListaResponseDTO toResponse(Lista lista) {
        return new ListaResponseDTO(lista.getId(), lista.getNombre());
    }
}