package com.example.tp1.security;

import com.example.tp1.domain.RolUsuario;

public record CurrentUser(Long id, String email, RolUsuario rol) {
    public boolean esAdmin() { return rol == RolUsuario.ADMIN; }
}
