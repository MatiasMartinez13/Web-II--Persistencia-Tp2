package com.example.tp1.persistence.repository;

import com.example.tp1.persistence.entity.ListaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListaJpaRepository extends JpaRepository<ListaEntity, Long> {
}