package com.tulicoreria.licoreria.security;

import com.tulicoreria.licoreria.model.Rol;
import com.tulicoreria.licoreria.model.Usuario;
import com.tulicoreria.licoreria.repository.RolRepository;
import com.tulicoreria.licoreria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        crearRoles();
        crearAdminPorDefecto();
        crearUsuarioTiendaOnline();
    }

    // ── Crea los 3 roles si no existen ───────────────────────────────────────
    private void crearRoles() {
        crearRolSiNoExiste("ROLE_ADMIN",    "Administrador — acceso total al sistema");
        crearRolSiNoExiste("ROLE_VENDEDOR", "Vendedor — acceso a ventas y clientes");
        crearRolSiNoExiste("ROLE_ALMACEN",  "Almacén — acceso a inventario y compras");
        log.info("✅ Roles cargados correctamente");
    }

    private void crearRolSiNoExiste(String nombre, String descripcion) {
        if (!rolRepository.existsByNombre(nombre)) {
            rolRepository.save(Rol.builder()
                    .nombre(nombre)
                    .descripcion(descripcion)
                    .build());
        }
    }

    // ── Crea el usuario admin por defecto si no existe ───────────────────────
    private void crearAdminPorDefecto() {
        if (!usuarioRepository.existsByUsername("admin")) {
            Rol rolAdmin = rolRepository.findByNombre("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("Rol ADMIN no encontrado"));

            Usuario admin = Usuario.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .nombreCompleto("Administrador")
                    .correo("admin@licoreria.com")
                    .activo(true)
                    .roles(Set.of(rolAdmin))
                    .build();

            usuarioRepository.save(admin);
            log.info("✅ Usuario admin creado — user: admin / pass: admin123");
            log.warn("⚠️  Cambia la contraseña del admin antes de pasar a producción");
        }
    }

    // ── Crea el usuario sistema para pedidos web ──────────────────────────────
    // Este usuario representa el canal de venta online (tienda pública).
    // Las ventas generadas desde el carrito web quedan registradas con este vendedor.
    @SuppressWarnings("unused")
    private void crearUsuarioTiendaOnline() {
        if (!usuarioRepository.existsByUsername("tienda_online")) {
            Rol rolVendedor = rolRepository.findByNombre("ROLE_VENDEDOR")
                    .orElseThrow(() -> new RuntimeException("Rol VENDEDOR no encontrado"));

            Usuario sistema = Usuario.builder()
                    .username("tienda_online")
                    .password(passwordEncoder.encode(java.util.UUID.randomUUID().toString()))
                    .nombreCompleto("Tienda Online")
                    .correo("tienda@licoreria.com")
                    .activo(true)
                    .roles(Set.of(rolVendedor))
                    .build();

            usuarioRepository.save(sistema);
            log.info("✅ Usuario sistema 'tienda_online' creado para pedidos web");
        }
    }
}
