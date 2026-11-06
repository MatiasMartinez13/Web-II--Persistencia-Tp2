package com.example.tp1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String emailUnico(String prefijo) {
        return prefijo + "-" + UUID.randomUUID() + "@example.com";
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

    private Long crearListaCon(String tk, String nombre) throws Exception {
        MvcResult r = mockMvc.perform(post("/api/listas")
                        .header("Authorization", "Bearer " + tk)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nombre": "%s" }
                                """.formatted(nombre)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void registro_devuelve201() throws Exception {
        String email = emailUnico("registro");
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "%s", "password": "secreta123" }
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.rol").value("USER"));
    }

    @Test
    void registro_emailDuplicado_devuelve409() throws Exception {
        String email = emailUnico("duplicado");
        String body = """
                { "email": "%s", "password": "secreta123" }
                """.formatted(email);
        mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void login_credencialesValidas_devuelve200ConToken() throws Exception {
        String email = emailUnico("login-ok");
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "%s", "password": "secreta123" }
                                """.formatted(email)))
                .andExpect(status().isCreated());

        MvcResult r = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "%s", "password": "secreta123" }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();
        String token = objectMapper.readTree(r.getResponse().getContentAsString()).get("token").asText();
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void login_credencialesInvalidas_devuelve401() throws Exception {
        String email = emailUnico("login-mal");
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "%s", "password": "secreta123" }
                                """.formatted(email)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "%s", "password": "incorrecta99" }
                                """.formatted(email)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void acceder_listaPropia_devuelve200() throws Exception {
        String tk = registrarYObtenerToken(emailUnico("propia"), "secreta123");
        Long listaId = crearListaCon(tk, "Propia");

        mockMvc.perform(get("/api/listas/{id}", listaId).header("Authorization", "Bearer " + tk))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Propia"));
    }

    @Test
    void acceder_listaAjena_devuelve403() throws Exception {
        String dueno = registrarYObtenerToken(emailUnico("dueno"), "secreta123");
        String intruso = registrarYObtenerToken(emailUnico("intruso"), "secreta123");
        Long listaId = crearListaCon(dueno, "Ajena");

        mockMvc.perform(get("/api/listas/{id}", listaId).header("Authorization", "Bearer " + intruso))
                .andExpect(status().isForbidden());
    }

    @Test
    void acceder_listasSinToken_devuelve401() throws Exception {
        mockMvc.perform(get("/api/listas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void acceder_adminListaAjena_devuelve200() throws Exception {
        String dueno = registrarYObtenerToken(emailUnico("dueno-admin"), "secreta123");
        Long listaId = crearListaCon(dueno, "AjenaAdmin");

        String emailAdmin = emailUnico("admin");
        jdbc.update("INSERT INTO usuarios (email, password_hash, rol) VALUES (?, ?, 'ADMIN')",
                emailAdmin, passwordEncoder.encode("admin12345"));
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "%s", "password": "admin12345" }
                                """.formatted(emailAdmin)))
                .andExpect(status().isOk())
                .andReturn();
        String adminToken = objectMapper.readTree(login.getResponse().getContentAsString()).get("token").asText();

        mockMvc.perform(get("/api/listas/{id}", listaId).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void productos_publico_devuelve200() throws Exception {
        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk());
    }
}
