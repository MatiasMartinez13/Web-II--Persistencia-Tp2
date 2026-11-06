package com.example.tp1.service;

import com.example.tp1.domain.Favorito;
import com.example.tp1.domain.Lista;
import com.example.tp1.dto.favorito.FavoritoResponseDTO;
import com.example.tp1.dto.lista.ListaRequestDTO;
import com.example.tp1.dto.lista.ListaResponseDTO;
import com.example.tp1.exception.AccessDeniedException;
import com.example.tp1.exception.ListaNoVaciaException;
import com.example.tp1.exception.ListaNotFoundException;
import com.example.tp1.mapper.FavoritoMapper;
import com.example.tp1.mapper.ListaMapper;
import com.example.tp1.repository.FavoritoRepository;
import com.example.tp1.repository.ListaRepository;
import com.example.tp1.security.CurrentUser;
import com.example.tp1.security.SecurityHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListaService {

    private final ListaRepository listaRepository;
    private final FavoritoRepository favoritoRepository;
    private final SecurityHelper securityHelper;

    public ListaService(ListaRepository listaRepository, FavoritoRepository favoritoRepository, SecurityHelper securityHelper) {
        this.listaRepository = listaRepository;
        this.favoritoRepository = favoritoRepository;
        this.securityHelper = securityHelper;
    }

    public ListaResponseDTO crear(ListaRequestDTO dto) {
        Lista lista = ListaMapper.toEntity(dto);
        lista.setUsuarioId(securityHelper.requireUser().id());
        return ListaMapper.toResponse(listaRepository.save(lista));
    }

    public List<ListaResponseDTO> listar() {
        CurrentUser actual = securityHelper.requireUser();
        List<Lista> listas = actual.esAdmin()
                ? listaRepository.findAll()
                : listaRepository.findByUsuarioId(actual.id());
        return listas.stream().map(ListaMapper::toResponse).toList();
    }

    public ListaResponseDTO obtener(Long id) {
        return ListaMapper.toResponse(obtenerEntidad(id));
    }

    public void eliminar(Long id) {
        Lista lista = obtenerEntidad(id);
        if (!favoritoRepository.findByListaId(id).isEmpty()) {
            throw new ListaNoVaciaException(
                    "No se puede eliminar la lista '" + lista.getNombre() + "' porque aún tiene favoritos vinculados");
        }
        listaRepository.deleteById(id);
    }

    public List<FavoritoResponseDTO> listarFavoritos(Long listaId) {
        obtenerEntidad(listaId);
        return favoritoRepository.findByListaId(listaId).stream()
                .map(FavoritoMapper::toResponse)
                .toList();
    }

    @Transactional
    public ListaResponseDTO moverFavoritos(Long origenId, Long destinoId) {
        if (origenId.equals(destinoId)) {
            throw new IllegalArgumentException("La lista origen y la lista destino deben ser distintas");
        }
        Lista origen = obtenerEntidad(origenId);
        Lista destino = obtenerEntidad(destinoId);
        verificarAcceso(destino);

        List<Favorito> favoritos = favoritoRepository.findByListaId(origenId);
        for (Favorito favorito : favoritos) {
            favorito.setListaId(destinoId);
            favoritoRepository.save(favorito);
        }

        if (!listaRepository.deleteById(origen.getId())) {
            throw new ListaNotFoundException("No existe una lista con el id " + origenId);
        }
        return ListaMapper.toResponse(destino);
    }

    private Lista obtenerEntidad(Long id) {
        Lista lista = listaRepository.findById(id)
                .orElseThrow(() -> new ListaNotFoundException("No existe una lista con el id " + id));
        verificarAcceso(lista);
        return lista;
    }

    private void verificarAcceso(Lista lista) {
        CurrentUser actual = securityHelper.requireUser();
        if (!actual.esAdmin() && !actual.id().equals(lista.getUsuarioId())) {
            throw new AccessDeniedException("No tenés acceso a la lista " + lista.getId());
        }
    }
}