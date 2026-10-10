package com.example.tp1.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de integración contra PostgreSQL real.
 * Usan Docker (Testcontainers); si Docker no está disponible se omiten en lugar de fallar.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
class FavoritoApiTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void crearDevuelve201() throws Exception {
        Long listaId = crearLista();

        mockMvc.perform(post("/api/favoritos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productoId": 1,
                                  "nota": "Ideal para regalar",
                                  "listaId": %d
                                }
                                """.formatted(listaId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productoId").value(1))
                .andExpect(jsonPath("$.nota").value("Ideal para regalar"))
                .andExpect(jsonPath("$.listaId").value(listaId))
                .andExpect(jsonPath("$.fechaAgregado").isNotEmpty());
    }

    @Test
    void crearConListaInexistenteDevuelve404() throws Exception {
        mockMvc.perform(post("/api/favoritos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productoId": 1,
                                  "nota": "Nota",
                                  "listaId": 9999
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe una lista con el id 9999"));
    }

    @Test
    void crearConListaIdObligatorioDevuelve400() throws Exception {
        mockMvc.perform(post("/api/favoritos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productoId": 1,
                                  "nota": "Nota"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.listaId").exists());
    }

    @Test
    void crearInvalidoDevuelve400ConDetalleDeCampos() throws Exception {
        mockMvc.perform(post("/api/favoritos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productoId": -5,
                                  "nota": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.productoId").exists())
                .andExpect(jsonPath("$.fieldErrors.nota").exists());
    }

    @Test
    void cuerpoMalformadoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/favoritos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\": \"no-es-un-numero\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crudCompleto() throws Exception {
        Long listaId = crearLista();

        MvcResult creado = mockMvc.perform(post("/api/favoritos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "productoId": 10, "nota": "Nota inicial", "listaId": %d }
                                """.formatted(listaId)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(creado.getResponse().getContentAsString());
        String id = json.get("id").asText();

        mockMvc.perform(get("/api/favoritos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/favoritos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(Integer.parseInt(id)))
                .andExpect(jsonPath("$.nota").value("Nota inicial"));

        mockMvc.perform(put("/api/favoritos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "productoId": 20, "nota": "Nota actualizada", "listaId": %d }
                                """.formatted(listaId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productoId").value(20))
                .andExpect(jsonPath("$.nota").value("Nota actualizada"));

        mockMvc.perform(delete("/api/favoritos/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/favoritos/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void obtenerInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/favoritos/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe un favorito con el id 9999"));
    }

    @Test
    void eliminarInexistenteDevuelve404() throws Exception {
        mockMvc.perform(delete("/api/favoritos/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void idNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/favoritos/abc"))
                .andExpect(status().isBadRequest());
    }

    private Long crearLista() throws Exception {
        MvcResult respuesta = mockMvc.perform(post("/api/listas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nombre": "Lista de test" }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(respuesta.getResponse().getContentAsString()).get("id").asLong();
    }
}