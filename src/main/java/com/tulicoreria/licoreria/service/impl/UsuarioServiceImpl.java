package com.tulicoreria.licoreria.service.impl;

import com.tulicoreria.licoreria.exception.RecursoNoEncontradoException;
import com.tulicoreria.licoreria.exception.ReglaDeNegocioException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tulicoreria.licoreria.dto.UsuarioRequestDTO;
import com.tulicoreria.licoreria.dto.UsuarioResponseDTO;
import com.tulicoreria.licoreria.model.Rol;
import com.tulicoreria.licoreria.model.Usuario;
import com.tulicoreria.licoreria.repository.RolRepository;
import com.tulicoreria.licoreria.repository.UsuarioRepository;
import com.tulicoreria.licoreria.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService, UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    // Spring Security llama a este método en cada login
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        if (!usuario.isActivo()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + username);
        }

        // Mapeamos los roles directamente asegurando el formato limpio en mayúsculas.
        // Dado que en tu Base de Datos ya vienen como "ROLE_ADMIN", "ROLE_ALMACEN",
        // pasará el texto idéntico y limpio hacia el contexto de seguridad.
        List<SimpleGrantedAuthority> authorities = usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority(rol.getNombre().toUpperCase().trim()))
                .toList();

        return new User(usuario.getUsername(), usuario.getPassword(), authorities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        return toDTO(usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id: " + id)));
    }

    @Override
    @Transactional
    public UsuarioResponseDTO crear(UsuarioRequestDTO dto) {
        if (usuarioRepository.existsByUsername(dto.getUsername())) {
            throw new ReglaDeNegocioException("El username ya está en uso: " + dto.getUsername());
        }
        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new ReglaDeNegocioException("El correo ya está registrado: " + dto.getCorreo());
        }
        if (dto.getRolId() == null) {
            throw new ReglaDeNegocioException("Debes seleccionar un rol de acceso para el usuario.");
        }
        Rol rol = rolRepository.findById(dto.getRolId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado con id: " + dto.getRolId()));

        // ROLE_CLIENTE es exclusivo de cuentas de la tienda web (ClienteWeb) y no
        // debe asignarse a personal interno, aunque llegue directo por request.
        if ("ROLE_CLIENTE".equals(rol.getNombre())) {
            throw new ReglaDeNegocioException("El rol Cliente no puede asignarse a cuentas de personal interno.");
        }

        Usuario usuario = Usuario.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nombreCompleto(dto.getNombreCompleto())
                .correo(dto.getCorreo())
                .activo(true)
                .roles(Set.of(rol))
                .build();

        return toDTO(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id: " + id));
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    private UsuarioResponseDTO toDTO(Usuario u) {
        return UsuarioResponseDTO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .nombreCompleto(u.getNombreCompleto())
                .correo(u.getCorreo())
                .activo(u.isActivo())
                .roles(u.getRoles().stream().map(Rol::getNombre).collect(Collectors.toSet()))
                .build();
    }
}
