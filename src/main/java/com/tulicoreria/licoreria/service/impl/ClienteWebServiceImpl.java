package com.tulicoreria.licoreria.service.impl;

import com.tulicoreria.licoreria.dto.RegistroClienteWebDTO;
import com.tulicoreria.licoreria.model.Cliente;
import com.tulicoreria.licoreria.model.ClienteWeb;
import com.tulicoreria.licoreria.model.DireccionEnvio;
import com.tulicoreria.licoreria.repository.ClienteWebRepository;
import com.tulicoreria.licoreria.repository.DireccionEnvioRepository;
import com.tulicoreria.licoreria.service.ClienteWebService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ClienteWebServiceImpl implements ClienteWebService {

    private final ClienteWebRepository clienteWebRepository;
    private final DireccionEnvioRepository direccionEnvioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public ClienteWeb registrar(RegistroClienteWebDTO dto) {
        if (clienteWebRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("El correo ya está registrado: " + dto.getEmail());
        }
        if (!dto.getPassword().equals(dto.getConfirmarPassword())) {
            throw new RuntimeException("Las contraseñas no coinciden.");
        }

        // Crear el registro de negocio (Cliente) para poder vincular ventas
        Cliente cliente = Cliente.builder()
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .tipoDocumento(Cliente.TipoDocumento.DNI)
                .numeroDocumento("WEB-" + System.currentTimeMillis())
                .telefono(dto.getTelefono())
                .correo(dto.getEmail())
                .fechaNacimiento(dto.getFechaNacimiento())
                .build();

        ClienteWeb cw = ClienteWeb.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .telefono(dto.getTelefono())
                .fechaNacimiento(dto.getFechaNacimiento())
                .activo(true)
                .fechaRegistro(LocalDateTime.now())
                .cliente(cliente)
                .build();

        return clienteWebRepository.save(cw);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteWeb findByEmail(String email) {
        return clienteWebRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Cliente web no encontrado: " + email));
    }

    @Override
    @Transactional
    public void agregarDireccion(String email, DireccionEnvio direccion) {
        ClienteWeb cw = findByEmail(email);

        // Si es la primera dirección, marcarla como default
        if (cw.getDirecciones().isEmpty()) {
            direccion.setEsDefault(true);
        }
        direccion.setClienteWeb(cw);
        cw.getDirecciones().add(direccion);
        clienteWebRepository.save(cw);
    }

    @Override
    @Transactional
    public void eliminarDireccion(String email, Long direccionId) {
        ClienteWeb cw = findByEmail(email);
        DireccionEnvio dir = direccionEnvioRepository.findById(direccionId)
                .orElseThrow(() -> new RuntimeException("Dirección no encontrada"));

        if (!dir.getClienteWeb().getId().equals(cw.getId())) {
            throw new RuntimeException("No tienes permiso para eliminar esta dirección.");
        }

        boolean eraDefault = dir.isEsDefault();
        cw.getDirecciones().remove(dir);
        direccionEnvioRepository.delete(dir);

        // Si era default, asignar la primera restante como nuevo default
        if (eraDefault && !cw.getDirecciones().isEmpty()) {
            DireccionEnvio primera = cw.getDirecciones().get(0);
            primera.setEsDefault(true);
            direccionEnvioRepository.save(primera);
        }
    }

    @Override
    @Transactional
    public void marcarDefaultDireccion(String email, Long direccionId) {
        ClienteWeb cw = findByEmail(email);
        cw.getDirecciones().forEach(d -> {
            d.setEsDefault(d.getId().equals(direccionId));
            direccionEnvioRepository.save(d);
        });
    }

    @Override
    @Transactional
    public void actualizarPerfil(String email, String nombre, String apellido, String telefono) {
        ClienteWeb cw = findByEmail(email);
        cw.setNombre(nombre);
        cw.setApellido(apellido);
        cw.setTelefono(telefono);

        // Sincronizar con el cliente de negocio
        if (cw.getCliente() != null) {
            cw.getCliente().setNombre(nombre);
            cw.getCliente().setApellido(apellido);
            cw.getCliente().setTelefono(telefono);
        }

        clienteWebRepository.save(cw);
    }
}
