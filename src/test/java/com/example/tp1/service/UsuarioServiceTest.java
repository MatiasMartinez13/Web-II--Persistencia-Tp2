package com.example.tp1.service;

import com.example.tp1.dto.auth.RegistroRequestDTO;
import com.example.tp1.exception.EmailAlreadyExistsException;
import com.example.tp1.security.JwtService;
import com.example.tp1.testutil.FakeRepositories.FakeUsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;

class UsuarioServiceTest {

    private FakeUsuarioRepository usuarioRepository;
    private PasswordEncoder encoder;
    private UsuarioService service;

    @BeforeEach
    void setUp() {
        usuarioRepository = new FakeUsuarioRepository();
        encoder = new BCryptPasswordEncoder();
        JwtService jwt = new JwtService("clave-local-solo-para-desarrollo-no-uso-en-produccion-1234567890", 3600000);
        service = new UsuarioService(usuarioRepository, encoder, jwt);
    }

    @Test
    void registrar_usuarioNuevo_resultadoNoGuardaPasswordEnPlano() {
        var response = service.registrar(new RegistroRequestDTO("persona@example.com", "secreta123"));

        assertThat(response.email()).isEqualTo("persona@example.com");
        assertThat(response.rol()).isEqualTo("USER");
        var guardado = usuarioRepository.findByEmail("persona@example.com").orElseThrow();
        assertThat(guardado.getPasswordHash()).isNotEqualTo("secreta123");
        assertThat(encoder.matches("secreta123", guardado.getPasswordHash())).isTrue();
    }

    @Test
    void registrar_emailExistente_resultadoLanza409() {
        service.registrar(new RegistroRequestDTO("persona@example.com", "secreta123"));

        assertThatThrownBy(() -> service.registrar(new RegistroRequestDTO("persona@example.com", "otra12345")))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void login_credencialesValidas_resultadoToken() {
        service.registrar(new RegistroRequestDTO("persona@example.com", "secreta123"));

        String token = service.login(new com.example.tp1.dto.auth.LoginRequestDTO("persona@example.com", "secreta123"));

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void login_credencialesInvalidas_resultadoLanza401() {
        service.registrar(new RegistroRequestDTO("persona@example.com", "secreta123"));

        assertThatThrownBy(() -> service.login(new com.example.tp1.dto.auth.LoginRequestDTO("persona@example.com", "incorrecta")))
                .isInstanceOf(com.example.tp1.exception.BadCredentialsException.class);
    }
}
