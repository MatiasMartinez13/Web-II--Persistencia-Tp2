package com.example.tp1.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DummyJsonPagina(
        List<DummyJsonProducto> products,
        int total,
        int skip,
        int limit
) {
}