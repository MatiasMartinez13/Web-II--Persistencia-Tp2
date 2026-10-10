package com.example.tp1.service;

import com.example.tp1.domain.Favorito;
import com.example.tp1.dto.favorito.FavoritoRequestDTO;
import com.example.tp1.dto.favorito.FavoritoResponseDTO;
import com.example.tp1.exception.FavoritoNotFoundException;
import com.example.tp1.exception.ListaNotFoundException;
import com.example.tp1.mapper.FavoritoMapper;
import com.example.tp1.repository.FavoritoRepository;
import com.example.tp1.repository.ListaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoritoService {

    private final FavoritoRepository repository;
    private final ListaRepository listaRepository;

    public FavoritoService(FavoritoRepository repository, ListaRepository listaRepository) {
        this.repository = repository;
        this.listaRepository = listaRepository;
    }

    public FavoritoResponseDTO crear(FavoritoRequestDTO dto) {
        validarLista(dto.listaId());
        Favorito favorito = FavoritoMapper.toEntity(dto);
        return FavoritoMapper.toResponse(repository.save(favorito));
    }

    public List<FavoritoResponseDTO> listar() {
        return repository.findAll().stream()
                .map(FavoritoMapper::toResponse)
                .toList();
    }

    public FavoritoResponseDTO obtener(Long id) {
        return FavoritoMapper.toResponse(obtenerEntidad(id));
    }

    public FavoritoResponseDTO actualizar(Long id, FavoritoRequestDTO dto) {
        validarLista(dto.listaId());
        Favorito favorito = obtenerEntidad(id);
        FavoritoMapper.applyTo(favorito, dto);
        return FavoritoMapper.toResponse(repository.save(favorito));
    }

    public void eliminar(Long id) {
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
}