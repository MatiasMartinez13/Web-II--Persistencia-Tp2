package com.example.tp1.persistence.adapter;

import com.example.tp1.domain.Favorito;
import com.example.tp1.persistence.entity.FavoritoEntity;
import com.example.tp1.persistence.entity.ListaEntity;
import com.example.tp1.persistence.repository.FavoritoJpaRepository;
import com.example.tp1.repository.FavoritoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class FavoritoRepositoryAdapter implements FavoritoRepository {

    private final FavoritoJpaRepository jpaRepository;

    public FavoritoRepositoryAdapter(FavoritoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Favorito save(Favorito favorito) {
        if (favorito.getId() == null) {
            favorito.setFechaAgregado(java.time.LocalDateTime.now());
        }
        FavoritoEntity entidad = jpaRepository.save(toEntity(favorito));
        return toDomain(entidad);
    }

    @Override
    public Optional<Favorito> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Favorito> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Favorito> findByListaId(Long listaId) {
        return jpaRepository.findByListaId(listaId).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean deleteById(Long id) {
        if (!jpaRepository.existsById(id)) {
            return false;
        }
        jpaRepository.deleteById(id);
        return true;
    }

    private FavoritoEntity toEntity(Favorito dominio) {
        FavoritoEntity entidad = new FavoritoEntity();
        entidad.setId(dominio.getId());
        entidad.setProductoId(dominio.getProductoId());
        entidad.setNota(dominio.getNota());
        entidad.setFechaAlta(dominio.getFechaAgregado());

        ListaEntity lista = new ListaEntity();
        lista.setId(dominio.getListaId());
        entidad.setLista(lista);
        return entidad;
    }

    private Favorito toDomain(FavoritoEntity entidad) {
        Favorito dominio = new Favorito();
        dominio.setId(entidad.getId());
        dominio.setProductoId(entidad.getProductoId());
        dominio.setNota(entidad.getNota());
        dominio.setFechaAgregado(entidad.getFechaAlta());
        if (entidad.getLista() != null) {
            dominio.setListaId(entidad.getLista().getId());
        }
        return dominio;
    }
}