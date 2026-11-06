package com.example.tp1.persistence.repository;

import com.example.tp1.persistence.entity.ListaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListaJpaRepository extends JpaRepository<ListaEntity, Long> {
    List<ListaEntity> findByUsuarioId(Long usuarioId);
}