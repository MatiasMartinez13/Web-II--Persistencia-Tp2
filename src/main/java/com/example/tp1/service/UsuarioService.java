package com.example.tp1.service;

import com.example.tp1.domain.RolUsuario;
import com.example.tp1.domain.Usuario;
import com.example.tp1.dto.auth.LoginRequestDTO;
import com.example.tp1.dto.auth.RegistroRequestDTO;
import com.example.tp1.dto.auth.UsuarioResponseDTO;
import com.example.tp1.exception.BadCredentialsException;
import com.example.tp1.exception.EmailAlreadyExistsException;
import com.example.tp1.repository.UsuarioRepository;
import com.example.tp1.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UsuarioResponseDTO registrar(RegistroRequestDTO dto) {
        String email = dto.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Ya existe un usuario con el email " + email);
        }
        Usuario usuario = new Usuario(null, email, passwordEncoder.encode(dto.password()), RolUsuario.USER);
        Usuario guardado = usuarioRepository.save(usuario);
        return new UsuarioResponseDTO(guardado.getId(), guardado.getEmail(), guardado.getRol().name());
    }

    public String login(LoginRequestDTO dto) {
        String email = dto.email().trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
        if (!passwordEncoder.matches(dto.password(), usuario.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
        return jwtService.generarToken(usuario);
    }
}
