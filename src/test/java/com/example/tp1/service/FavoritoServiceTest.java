package com.example.tp1.service;

import com.example.tp1.dto.favorito.FavoritoRequestDTO;
import com.example.tp1.dto.favorito.FavoritoResponseDTO;
import com.example.tp1.exception.FavoritoNotFoundException;
import com.example.tp1.exception.ListaNotFoundException;
import com.example.tp1.testutil.FakeRepositories.FakeFavoritoRepository;
import com.example.tp1.testutil.FakeRepositories.FakeListaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FavoritoServiceTest {

    private FakeFavoritoRepository favoritoRepository;
    private FakeListaRepository listaRepository;
    private FavoritoService service;

    private Long listaId;

    @BeforeEach
    void setUp() {
        favoritoRepository = new FakeFavoritoRepository();
        listaRepository = new FakeListaRepository();
        service = new FavoritoService(favoritoRepository, listaRepository);
        listaId = listaRepository.seed("Regalos").getId();
    }

    @Test
    void crearAsignaIdYFecha() {
        FavoritoResponseDTO creado = service.crear(new FavoritoRequestDTO(1L, "Ideal para regalar", listaId));

        assertThat(creado.id()).isEqualTo(1L);
        assertThat(creado.productoId()).isEqualTo(1L);
        assertThat(creado.nota()).isEqualTo("Ideal para regalar");
        assertThat(creado.fechaAgregado()).isNotNull();
        assertThat(creado.listaId()).isEqualTo(listaId);
    }

    @Test
    void crearConListaInexistenteLanza404() {
        assertThatThrownBy(() -> service.crear(new FavoritoRequestDTO(1L, "Nota", 999L)))
                .isInstanceOf(ListaNotFoundException.class);
    }

    @Test
    void listarDevuelveTodos() {
        service.crear(new FavoritoRequestDTO(1L, "Una", listaId));
        service.crear(new FavoritoRequestDTO(2L, "Dos", listaId));

        List<FavoritoResponseDTO> favoritos = service.listar();

        assertThat(favoritos).hasSize(2);
    }

    @Test
    void obtenerPorId() {
        FavoritoResponseDTO creado = service.crear(new FavoritoRequestDTO(1L, "Una", listaId));

        FavoritoResponseDTO obtenido = service.obtener(creado.id());

        assertThat(obtenido.id()).isEqualTo(creado.id());
        assertThat(obtenido.nota()).isEqualTo("Una");
    }

    @Test
    void obtenerInexistenteLanza404() {
        assertThatThrownBy(() -> service.obtener(999L))
                .isInstanceOf(FavoritoNotFoundException.class);
    }

    @Test
    void actualizarMantieneIdYFecha() {
        FavoritoResponseDTO creado = service.crear(new FavoritoRequestDTO(1L, "Antes", listaId));

        FavoritoResponseDTO actualizado = service.actualizar(creado.id(), new FavoritoRequestDTO(5L, "Después", listaId));

        assertThat(actualizado.id()).isEqualTo(creado.id());
        assertThat(actualizado.productoId()).isEqualTo(5L);
        assertThat(actualizado.nota()).isEqualTo("Después");
        assertThat(actualizado.fechaAgregado()).isEqualTo(creado.fechaAgregado());
    }

    @Test
    void actualizarConListaInexistenteLanza404() {
        FavoritoResponseDTO creado = service.crear(new FavoritoRequestDTO(1L, "Antes", listaId));

        assertThatThrownBy(() -> service.actualizar(creado.id(), new FavoritoRequestDTO(5L, "Después", 999L)))
                .isInstanceOf(ListaNotFoundException.class);
    }

    @Test
    void eliminarQuitaElFavorito() {
        FavoritoResponseDTO creado = service.crear(new FavoritoRequestDTO(1L, "Una", listaId));

        service.eliminar(creado.id());

        assertThat(service.listar()).isEmpty();
        assertThatThrownBy(() -> service.eliminar(creado.id()))
                .isInstanceOf(FavoritoNotFoundException.class);
    }
}