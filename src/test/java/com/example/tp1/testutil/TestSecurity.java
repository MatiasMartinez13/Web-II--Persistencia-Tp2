package com.example.tp1.testutil;

import com.example.tp1.domain.RolUsuario;
import com.example.tp1.security.CurrentUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

public final class TestSecurity {
    private TestSecurity() {}
    public static void authenticateAs(Long id, String email, RolUsuario rol) {
        CurrentUser principal = new CurrentUser(id, email, rol);
        var auth = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol.name())));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
    public static void clear() { SecurityContextHolder.clearContext(); }
}
