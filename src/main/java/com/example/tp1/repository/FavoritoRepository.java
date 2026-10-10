package com.example.tp1.repository;

import com.example.tp1.domain.Favorito;

import java.util.List;
import java.util.Optional;

public interface FavoritoRepository {

    Favorito save(Favorito favorito);

    Optional<Favorito> findById(Long id);

    List<Favorito> findAll();

    List<Favorito> findByListaId(Long listaId);

    boolean deleteById(Long id);
}