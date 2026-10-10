package com.example.tp1.mapper;

import com.example.tp1.domain.Favorito;
import com.example.tp1.dto.favorito.FavoritoRequestDTO;
import com.example.tp1.dto.favorito.FavoritoResponseDTO;

public final class FavoritoMapper {

    private FavoritoMapper() {
    }

    public static Favorito toEntity(FavoritoRequestDTO dto) {
        Favorito favorito = new Favorito();
        favorito.setProductoId(dto.productoId());
        favorito.setNota(dto.nota());
        favorito.setListaId(dto.listaId());
        return favorito;
    }

    public static void applyTo(Favorito favorito, FavoritoRequestDTO dto) {
        favorito.setProductoId(dto.productoId());
        favorito.setNota(dto.nota());
        favorito.setListaId(dto.listaId());
    }

    public static FavoritoResponseDTO toResponse(Favorito favorito) {
        return new FavoritoResponseDTO(
                favorito.getId(),
                favorito.getProductoId(),
                favorito.getNota(),
                favorito.getFechaAgregado(),
                favorito.getListaId()
        );
    }
}