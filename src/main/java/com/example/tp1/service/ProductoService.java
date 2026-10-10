package com.example.tp1.service;

import com.example.tp1.client.DummyJsonClient;
import com.example.tp1.dto.producto.PaginaProductosDTO;
import com.example.tp1.dto.producto.ProductoDTO;
import com.example.tp1.exception.ProductoNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ProductoService {

    private final DummyJsonClient client;

    public ProductoService(DummyJsonClient client) {
        this.client = client;
    }

    public PaginaProductosDTO listar(int limit, int skip) {
        return client.listarProductos(limit, skip);
    }

    public ProductoDTO obtener(long id) {
        ProductoDTO producto = client.obtenerProducto(id);
        if (producto == null) {
            throw new ProductoNotFoundException("No existe un producto con el id " + id);
        }
        return producto;
    }
}