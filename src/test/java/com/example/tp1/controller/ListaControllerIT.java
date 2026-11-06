package com.example.tp1.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
class ListaControllerIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        token = registrarYObtenerToken("user-" + UUID.randomUUID() + "@example.com", "secreta123");
    }

    private String auth() { return "Bearer " + token; }

    @Test
    void crearDevuelve201() throws Exception {
        mockMvc.perform(post("/api/listas")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nombre": "Regalos" }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value("Regalos"));
    }

    @Test
    void crearSinNombreDevuelve400() throws Exception {
        mockMvc.perform(post("/api/listas")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nombre": "  " }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nombre").exists());
    }

    @Test
    void listarDevuelve200() throws Exception {
        mockMvc.perform(get("/api/listas").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void obtenerDevuelve200Y404() throws Exception {
        Long id = crearLista("Regalos");

        mockMvc.perform(get("/api/listas/{id}", id).header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Regalos"));

        mockMvc.perform(get("/api/listas/9999").header("Authorization", auth()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe una lista con el id 9999"));
    }

    @Test
    void eliminarListaVaciaDevuelve204() throws Exception {
        Long id = crearLista("Vacia");

        mockMvc.perform(delete("/api/listas/{id}", id).header("Authorization", auth()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/listas/{id}", id).header("Authorization", auth()))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarListaConFavoritosDevuelve409() throws Exception {
        Long listaId = crearLista("ConFavoritos");
        crearFavorito(listaId);

        mockMvc.perform(delete("/api/listas/{id}", listaId).header("Authorization", auth()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("aún tiene favoritos")));
    }

    @Test
    void eliminarListaInexistenteDevuelve404() throws Exception {
        mockMvc.perform(delete("/api/listas/9999").header("Authorization", auth()))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarFavoritosDeUnaLista() throws Exception {
        Long listaId = crearLista("Regalos");
        crearFavorito(listaId);

        mockMvc.perform(get("/api/listas/{id}/favoritos", listaId).header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].listaId").value(listaId));
    }

    @Test
    void listarFavoritosDeListaInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/listas/9999/favoritos").header("Authorization", auth()))
                .andExpect(status().isNotFound());
    }

    @Test
    void moverFavoritosReasignaYEliminaOrigen() throws Exception {
        Long origenId = crearLista("Origen");
        Long destinoId = crearLista("Destino");
        String favoritoId = crearFavorito(origenId);

        mockMvc.perform(post("/api/listas/{id}/mover-favoritos", origenId)
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "destinoId": %d }
                                """.formatted(destinoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(destinoId));

        mockMvc.perform(get("/api/listas/{id}", origenId).header("Authorization", auth()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/listas/{id}/favoritos", destinoId).header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(Integer.parseInt(favoritoId)))
                .andExpect(jsonPath("$[0].listaId").value(destinoId));
    }

    @Test
    void moverFavoritosConOrigenIgualDestinoDevuelve400() throws Exception {
        Long listaId = crearLista("Unica");

        mockMvc.perform(post("/api/listas/{id}/mover-favoritos", listaId)
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "destinoId": %d }
                                """.formatted(listaId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void moverFavoritosConDestinoInexistenteDevuelve404() throws Exception {
        Long origenId = crearLista("Origen");
        crearFavorito(origenId);

        mockMvc.perform(post("/api/listas/{id}/mover-favoritos", origenId)
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "destinoId": 9999 }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void idNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/listas/abc").header("Authorization", auth()))
                .andExpect(status().isBadRequest());
    }

    private Long crearLista(String nombre) throws Exception {
        MvcResult respuesta = mockMvc.perform(post("/api/listas")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nombre": "%s" }
                                """.formatted(nombre)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(respuesta.getResponse().getContentAsString()).get("id").asLong();
    }

    private Long crearListaCon(String tk, String nombre) throws Exception {
        MvcResult respuesta = mockMvc.perform(post("/api/listas")
                        .header("Authorization", "Bearer " + tk)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nombre": "%s" }
                                """.formatted(nombre)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(respuesta.getResponse().getContentAsString()).get("id").asLong();
    }

    private String crearFavorito(Long listaId) throws Exception {
        MvcResult respuesta = mockMvc.perform(post("/api/favoritos")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "productoId": 1, "nota": "Nota", "listaId": %d }
                                """.formatted(listaId)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(respuesta.getResponse().getContentAsString());
        return json.get("id").asText();
    }

    private String registrarYObtenerToken(String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "%s", "password": "%s" }
                                """.formatted(email, password)))
                .andExpect(status().isCreated());
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "%s", "password": "%s" }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(login.getResponse().getContentAsString()).get("token").asText();
    }
}
