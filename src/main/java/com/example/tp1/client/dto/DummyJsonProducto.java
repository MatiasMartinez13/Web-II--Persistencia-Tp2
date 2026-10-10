package com.example.tp1.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DummyJsonProducto(
        long id,
        String title,
        String description,
        double price,
        String category,
        double rating,
        int stock,
        String thumbnail
) {
}