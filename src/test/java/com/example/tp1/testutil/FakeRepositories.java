package com.example.tp1.testutil;

import com.example.tp1.domain.Favorito;
import com.example.tp1.domain.Lista;
import com.example.tp1.domain.RolUsuario;
import com.example.tp1.domain.Usuario;
import com.example.tp1.repository.FavoritoRepository;
import com.example.tp1.repository.ListaRepository;
import com.example.tp1.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public final class FakeRepositories {

    private FakeRepositories() {
    }

    public static class FakeFavoritoRepository implements FavoritoRepository {

        private final Map<Long, Favorito> storage = new LinkedHashMap<>();
        private final AtomicLong secuencia = new AtomicLong(1);

        @Override
        public Favorito save(Favorito favorito) {
            if (favorito.getId() == null) {
                favorito.setId(secuencia.getAndIncrement());
                favorito.setFechaAgregado(LocalDateTime.now());
            }
            storage.put(favorito.getId(), favorito);
            return favorito;
        }

        @Override
        public Optional<Favorito> findById(Long id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public List<Favorito> findAll() {
            return List.copyOf(storage.values());
        }

        @Override
        public List<Favorito> findByListaId(Long listaId) {
            return storage.values().stream()
                    .filter(favorito -> listaId.equals(favorito.getListaId()))
                    .toList();
        }

        @Override
        public boolean deleteById(Long id) {
            return storage.remove(id) != null;
        }
    }

    public static class FakeListaRepository implements ListaRepository {

        private final Map<Long, Lista> storage = new LinkedHashMap<>();
        private final AtomicLong secuencia = new AtomicLong(1);

        @Override
        public Lista save(Lista lista) {
            if (lista.getId() == null) {
                lista.setId(secuencia.getAndIncrement());
            }
            storage.put(lista.getId(), lista);
            return lista;
        }

        public Lista seed(String nombre) {
            Lista lista = new Lista();
            lista.setNombre(nombre);
            lista.setUsuarioId(1L);
            return save(lista);
        }

        @Override
        public Optional<Lista> findById(Long id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public List<Lista> findAll() {
            return List.copyOf(storage.values());
        }

        @Override
        public List<Lista> findByUsuarioId(Long usuarioId) {
            return storage.values().stream()
                    .filter(l -> usuarioId.equals(l.getUsuarioId()))
                    .toList();
        }

        @Override
        public boolean deleteById(Long id) {
            return storage.remove(id) != null;
        }
    }

    public static class FakeUsuarioRepository implements UsuarioRepository {
        private final Map<Long, Usuario> storage = new LinkedHashMap<>();
        private final AtomicLong secuencia = new AtomicLong(1);

        @Override
        public Usuario save(Usuario usuario) {
            if (usuario.getId() == null) {
                usuario.setId(secuencia.getAndIncrement());
            }
            storage.put(usuario.getId(), usuario);
            return usuario;
        }

        @Override
        public Optional<Usuario> findById(Long id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public Optional<Usuario> findByEmail(String email) {
            return storage.values().stream()
                    .filter(u -> u.getEmail().equalsIgnoreCase(email))
                    .findFirst();
        }

        @Override
        public boolean existsByEmail(String email) {
            return findByEmail(email).isPresent();
        }
    }
}