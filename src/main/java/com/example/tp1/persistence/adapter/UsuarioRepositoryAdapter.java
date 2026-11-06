package com.example.tp1.persistence.adapter;

import com.example.tp1.domain.Usuario;
import com.example.tp1.persistence.entity.UsuarioEntity;
import com.example.tp1.persistence.repository.UsuarioJpaRepository;
import com.example.tp1.repository.UsuarioRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UsuarioRepositoryAdapter implements UsuarioRepository {
    private final UsuarioJpaRepository jpaRepository;
    public UsuarioRepositoryAdapter(UsuarioJpaRepository jpaRepository) { this.jpaRepository = jpaRepository; }

    @Override public Usuario save(Usuario u) { return toDomain(jpaRepository.save(toEntity(u))); }
    @Override public Optional<Usuario> findById(Long id) { return jpaRepository.findById(id).map(this::toDomain); }
    @Override public Optional<Usuario> findByEmail(String email) { return jpaRepository.findByEmail(email).map(this::toDomain); }
    @Override public boolean existsByEmail(String email) { return jpaRepository.existsByEmail(email); }

    private UsuarioEntity toEntity(Usuario d) {
        UsuarioEntity e = new UsuarioEntity();
        e.setId(d.getId()); e.setEmail(d.getEmail()); e.setPasswordHash(d.getPasswordHash()); e.setRol(d.getRol());
        return e;
    }
    private Usuario toDomain(UsuarioEntity e) { return new Usuario(e.getId(), e.getEmail(), e.getPasswordHash(), e.getRol()); }
}
