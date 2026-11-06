package com.example.tp1.service;

import com.example.tp1.domain.Favorito;
import com.example.tp1.domain.RolUsuario;
import com.example.tp1.dto.favorito.FavoritoResponseDTO;
import com.example.tp1.dto.lista.ListaRequestDTO;
import com.example.tp1.dto.lista.ListaResponseDTO;
import com.example.tp1.exception.AccessDeniedException;
import com.example.tp1.exception.ListaNoVaciaException;
import com.example.tp1.exception.ListaNotFoundException;
import com.example.tp1.security.SecurityHelper;
import com.example.tp1.testutil.FakeRepositories.FakeFavoritoRepository;
import com.example.tp1.testutil.FakeRepositories.FakeListaRepository;
import com.example.tp1.testutil.TestSecurity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ListaServiceImplTest {

    private FakeFavoritoRepository favoritoRepository;
    private FakeListaRepository listaRepository;
    private ListaService service;

    @BeforeEach
    void setUp() {
        favoritoRepository = new FakeFavoritoRepository();
        listaRepository = new FakeListaRepository();
        service = new ListaService(listaRepository, favoritoRepository, new SecurityHelper());
        TestSecurity.authenticateAs(1L, "user@example.com", RolUsuario.USER);
    }

    @AfterEach
    void tearDown() {
        TestSecurity.clear();
    }

    @Test
    void crear_asociaUsuarioAutenticado_resultadoListaConDueño() {
        ListaResponseDTO creada = service.crear(new ListaRequestDTO("Regalos"));

        assertThat(creada.id()).isEqualTo(1L);
        assertThat(creada.nombre()).isEqualTo("Regalos");
        assertThat(listaRepository.findById(creada.id()).orElseThrow().getUsuarioId()).isEqualTo(1L);
    }

    @Test
    void listar_devuelveSoloPropias_resultadoFiltradas() {
        service.crear(new ListaRequestDTO("Una"));
        TestSecurity.authenticateAs(2L, "otro@example.com", RolUsuario.USER);
        service.crear(new ListaRequestDTO("Ajena"));

        TestSecurity.authenticateAs(1L, "user@example.com", RolUsuario.USER);
        assertThat(service.listar()).hasSize(1);
        assertThat(service.listar().get(0).nombre()).isEqualTo("Una");
    }

    @Test
    void listar_admin_resultadoTodas() {
        service.crear(new ListaRequestDTO("Una"));
        TestSecurity.authenticateAs(2L, "otro@example.com", RolUsuario.USER);
        service.crear(new ListaRequestDTO("Ajena"));

        TestSecurity.authenticateAs(99L, "admin@example.com", RolUsuario.ADMIN);
        assertThat(service.listar()).hasSize(2);
    }

    @Test
    void obtener_listaAjena_lanzaAccesoDenegado() {
        TestSecurity.authenticateAs(2L, "otro@example.com", RolUsuario.USER);
        Long ajenaId = service.crear(new ListaRequestDTO("Ajena")).id();

        TestSecurity.authenticateAs(1L, "user@example.com", RolUsuario.USER);
        assertThatThrownBy(() -> service.obtener(ajenaId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void obtener_admin_listaAjena_resultadoPermite() {
        TestSecurity.authenticateAs(2L, "otro@example.com", RolUsuario.USER);
        Long ajenaId = service.crear(new ListaRequestDTO("Ajena")).id();

        TestSecurity.authenticateAs(99L, "admin@example.com", RolUsuario.ADMIN);
        assertThat(service.obtener(ajenaId).nombre()).isEqualTo("Ajena");
    }

    @Test
    void eliminarListaVacia() {
        Long id = service.crear(new ListaRequestDTO("Vacia")).id();

        service.eliminar(id);

        assertThat(service.listar()).isEmpty();
    }

    @Test
    void eliminarListaConFavoritosLanza409() {
        Long listaId = service.crear(new ListaRequestDTO("ConFavoritos")).id();
        favoritoRepository.save(favoritoConLista(listaId));

        assertThatThrownBy(() -> service.eliminar(listaId))
                .isInstanceOf(ListaNoVaciaException.class);
    }

    @Test
    void eliminarInexistenteLanza404() {
        assertThatThrownBy(() -> service.eliminar(999L))
                .isInstanceOf(ListaNotFoundException.class);
    }

    @Test
    void listarFavoritosDeUnaLista() {
        Long listaId = service.crear(new ListaRequestDTO("Regalos")).id();
        favoritoRepository.save(favoritoConLista(listaId));

        List<FavoritoResponseDTO> favoritos = service.listarFavoritos(listaId);

        assertThat(favoritos).hasSize(1);
        assertThat(favoritos.get(0).listaId()).isEqualTo(listaId);
    }

    @Test
    void listarFavoritosDeListaInexistenteLanza404() {
        assertThatThrownBy(() -> service.listarFavoritos(999L))
                .isInstanceOf(ListaNotFoundException.class);
    }

    @Test
    void moverFavoritosReasignaYEliminaOrigen() {
        Long origenId = service.crear(new ListaRequestDTO("Origen")).id();
        Long destinoId = service.crear(new ListaRequestDTO("Destino")).id();
        Favorito uno = favoritoRepository.save(favoritoConLista(origenId));
        Favorito dos = favoritoRepository.save(favoritoConLista(origenId));

        ListaResponseDTO destino = service.moverFavoritos(origenId, destinoId);

        assertThat(destino.id()).isEqualTo(destinoId);
        assertThatThrownBy(() -> service.obtener(origenId))
                .isInstanceOf(ListaNotFoundException.class);
        assertThat(favoritoRepository.findById(uno.getId()).orElseThrow().getListaId()).isEqualTo(destinoId);
        assertThat(favoritoRepository.findById(dos.getId()).orElseThrow().getListaId()).isEqualTo(destinoId);
        assertThat(service.listarFavoritos(destinoId)).hasSize(2);
    }

    @Test
    void moverFavoritosConOrigenIgualDestinoLanza400() {
        Long listaId = service.crear(new ListaRequestDTO("Unica")).id();

        assertThatThrownBy(() -> service.moverFavoritos(listaId, listaId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void moverFavoritosConOrigenInexistenteLanza404() {
        Long destinoId = service.crear(new ListaRequestDTO("Destino")).id();

        assertThatThrownBy(() -> service.moverFavoritos(999L, destinoId))
                .isInstanceOf(ListaNotFoundException.class);
    }

    private Favorito favoritoConLista(Long listaId) {
        Favorito favorito = new Favorito();
        favorito.setProductoId(1L);
        favorito.setNota("Nota");
        favorito.setListaId(listaId);
        return favorito;
    }
}
