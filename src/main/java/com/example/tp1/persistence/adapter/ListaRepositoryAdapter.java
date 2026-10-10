package com.example.tp1.persistence.adapter;

import com.example.tp1.domain.Lista;
import com.example.tp1.persistence.entity.ListaEntity;
import com.example.tp1.persistence.repository.ListaJpaRepository;
import com.example.tp1.repository.ListaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ListaRepositoryAdapter implements ListaRepository {

    private final ListaJpaRepository jpaRepository;

    public ListaRepositoryAdapter(ListaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Lista save(Lista lista) {
        return toDomain(jpaRepository.save(toEntity(lista)));
    }

    @Override
    public Optional<Lista> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Lista> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public boolean deleteById(Long id) {
        if (!jpaRepository.existsById(id)) {
            return false;
        }
        jpaRepository.deleteById(id);
        return true;
    }

    private ListaEntity toEntity(Lista dominio) {
        ListaEntity entidad = new ListaEntity();
        entidad.setId(dominio.getId());
        entidad.setNombre(dominio.getNombre());
        return entidad;
    }

    private Lista toDomain(ListaEntity entidad) {
        Lista dominio = new Lista();
        dominio.setId(entidad.getId());
        dominio.setNombre(entidad.getNombre());
        return dominio;
    }
}