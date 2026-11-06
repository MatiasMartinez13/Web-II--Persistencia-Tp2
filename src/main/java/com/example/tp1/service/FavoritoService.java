package com.example.tp1.service;

import com.example.tp1.domain.Favorito;
import com.example.tp1.dto.favorito.FavoritoRequestDTO;
import com.example.tp1.dto.favorito.FavoritoResponseDTO;
import com.example.tp1.exception.AccessDeniedException;
import com.example.tp1.exception.FavoritoNotFoundException;
import com.example.tp1.exception.ListaNotFoundException;
import com.example.tp1.mapper.FavoritoMapper;
import com.example.tp1.repository.FavoritoRepository;
import com.example.tp1.repository.ListaRepository;
import com.example.tp1.security.CurrentUser;
import com.example.tp1.security.SecurityHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoritoService {

    private final FavoritoRepository repository;
    private final ListaRepository listaRepository;
    private final SecurityHelper securityHelper;

    public FavoritoService(FavoritoRepository repository, ListaRepository listaRepository, SecurityHelper securityHelper) {
        this.repository = repository;
        this.listaRepository = listaRepository;
        this.securityHelper = securityHelper;
    }

    public FavoritoResponseDTO crear(FavoritoRequestDTO dto) {
        validarListaConAcceso(dto.listaId());
        Favorito favorito = FavoritoMapper.toEntity(dto);
        return FavoritoMapper.toResponse(repository.save(favorito));
    }

    public List<FavoritoResponseDTO> listar() {
        CurrentUser actual = securityHelper.requireUser();
        if (actual.esAdmin()) {
            return repository.findAll().stream()
                    .map(FavoritoMapper::toResponse)
                    .toList();
        }
        return repository.findAll().stream()
                .filter(f -> esDueno(f.getListaId(), actual))
                .map(FavoritoMapper::toResponse)
                .toList();
    }

    public FavoritoResponseDTO obtener(Long id) {
        Favorito favorito = obtenerEntidad(id);
        verificarAcceso(favorito);
        return FavoritoMapper.toResponse(favorito);
    }

    public FavoritoResponseDTO actualizar(Long id, FavoritoRequestDTO dto) {
        Favorito favorito = obtenerEntidad(id);
        verificarAcceso(favorito);
        validarListaConAcceso(dto.listaId());
        FavoritoMapper.applyTo(favorito, dto);
        return FavoritoMapper.toResponse(repository.save(favorito));
    }

    public void eliminar(Long id) {
        Favorito favorito = obtenerEntidad(id);
        verificarAcceso(favorito);
        if (!repository.deleteById(id)) {
            throw new FavoritoNotFoundException("No existe un favorito con el id " + id);
        }
    }

    private Favorito obtenerEntidad(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new FavoritoNotFoundException("No existe un favorito con el id " + id));
    }

    private void validarLista(Long listaId) {
        if (listaRepository.findById(listaId).isEmpty()) {
            throw new ListaNotFoundException("No existe una lista con el id " + listaId);
        }
    }

    private void validarListaConAcceso(Long listaId) {
        var lista = listaRepository.findById(listaId)
                .orElseThrow(() -> new ListaNotFoundException("No existe una lista con el id " + listaId));
        CurrentUser actual = securityHelper.requireUser();
        if (!actual.esAdmin() && !actual.id().equals(lista.getUsuarioId())) {
            throw new AccessDeniedException("No tenés acceso a la lista " + listaId);
        }
    }

    private void verificarAcceso(Favorito favorito) {
        CurrentUser actual = securityHelper.requireUser();
        if (!actual.esAdmin() && !esDueno(favorito.getListaId(), actual)) {
            throw new AccessDeniedException("No tenés acceso al favorito " + favorito.getId());
        }
    }

    private boolean esDueno(Long listaId, CurrentUser actual) {
        return listaRepository.findById(listaId)
                .map(l -> actual.id().equals(l.getUsuarioId()))
                .orElse(false);
    }
}