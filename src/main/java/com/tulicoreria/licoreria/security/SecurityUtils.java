package com.tulicoreria.licoreria.security;

import com.tulicoreria.licoreria.model.Usuario;
import com.tulicoreria.licoreria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UsuarioRepository usuarioRepository;

    // Obtiene el usuario autenticado actual como entidad
    public Usuario getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado: " + username));
    }

    // Obtiene solo el username del usuario autenticado
    public String getUsernameActual() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // Verifica si el usuario actual tiene un rol específico
    public boolean tieneRol(String rol) {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals(rol));
    }

    public boolean esAdmin() {
        return tieneRol("ROLE_ADMIN");
    }

    public boolean esVendedor() {
        return tieneRol("ROLE_VENDEDOR");
    }

    public boolean esAlmacen() {
        return tieneRol("ROLE_ALMACEN");
    }
}
