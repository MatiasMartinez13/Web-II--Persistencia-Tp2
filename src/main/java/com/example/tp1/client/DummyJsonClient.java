package com.example.tp1.client;

import com.example.tp1.client.dto.DummyJsonPagina;
import com.example.tp1.client.dto.DummyJsonProducto;
import com.example.tp1.dto.producto.PaginaProductosDTO;
import com.example.tp1.dto.producto.ProductoDTO;
import com.example.tp1.exception.ExternalApiException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Service
public class DummyJsonClient {

    private final RestClient restClient;

    public DummyJsonClient(@Qualifier("dummyJsonRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public PaginaProductosDTO listarProductos(int limit, int skip) {
        try {
            DummyJsonPagina pagina = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/products")
                            .queryParam("limit", limit)
                            .queryParam("skip", skip)
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        throw new ExternalApiException(
                                "La API externa DummyJSON respondió con el estado " + response.getStatusCode());
                    })
                    .body(DummyJsonPagina.class);

            if (pagina == null || pagina.products() == null) {
                throw new ExternalApiException("La API externa DummyJSON devolvió una respuesta vacía");
            }
            return mapearPagina(pagina);
        } catch (RestClientException ex) {
            throw new ExternalApiException("No se pudo consultar la API externa DummyJSON", ex);
        }
    }

    public ProductoDTO obtenerProducto(long id) {
        try {
            DummyJsonProducto producto = restClient.get()
                    .uri("/products/{id}", id)
                    .retrieve()
                    .body(DummyJsonProducto.class);

            return producto == null ? null : mapearProducto(producto);
        } catch (HttpClientErrorException.NotFound ex) {
            return null;
        } catch (RestClientException ex) {
            throw new ExternalApiException("No se pudo consultar la API externa DummyJSON", ex);
        }
    }

    private PaginaProductosDTO mapearPagina(DummyJsonPagina pagina) {
        List<ProductoDTO> productos = pagina.products().stream()
                .map(this::mapearProducto)
                .toList();
        int totalPaginas = pagina.limit() > 0
                ? (int) Math.ceil((double) pagina.total() / pagina.limit())
                : 0;
        return new PaginaProductosDTO(productos, pagina.total(), pagina.skip(), pagina.limit(), totalPaginas);
    }

    private ProductoDTO mapearProducto(DummyJsonProducto p) {
        return new ProductoDTO(
                p.id(),
                p.title(),
                p.description(),
                p.price(),
                p.category(),
                p.rating(),
                p.stock(),
                p.thumbnail()
        );
    }
}