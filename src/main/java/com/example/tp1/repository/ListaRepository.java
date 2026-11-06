package com.example.tp1.repository;

import com.example.tp1.domain.Lista;

import java.util.List;
import java.util.Optional;

public interface ListaRepository {

    Lista save(Lista lista);

    Optional<Lista> findById(Long id);

    List<Lista> findAll();

    List<Lista> findByUsuarioId(Long usuarioId);

    boolean deleteById(Long id);
}