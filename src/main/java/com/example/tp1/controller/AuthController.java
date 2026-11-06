package com.example.tp1.controller;

import com.example.tp1.dto.auth.LoginRequestDTO;
import com.example.tp1.dto.auth.LoginResponseDTO;
import com.example.tp1.dto.auth.RegistroRequestDTO;
import com.example.tp1.dto.auth.UsuarioResponseDTO;
import com.example.tp1.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro de usuarios y emisión de tokens JWT. Endpoints públicos: no requieren Authorization.")
public class AuthController {

    private final UsuarioService service;

    public AuthController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping("/registro")
    @Operation(summary = "Registra un usuario", description = "Crea un usuario con rol USER y contraseña hasheada. 409 si el email ya existe.")
    public ResponseEntity<UsuarioResponseDTO> registro(@Valid @RequestBody RegistroRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(dto));
    }

    @PostMapping("/login")
    @Operation(summary = "Inicia sesión", description = "Valida email y contraseña y devuelve un JWT. 401 si las credenciales son inválidas.")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO dto) {
        return new LoginResponseDTO(service.login(dto), "Bearer");
    }
}
