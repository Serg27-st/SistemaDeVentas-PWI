package com.tulicoreria.licoreria.service;

import com.tulicoreria.licoreria.dto.RegistroClienteWebDTO;
import com.tulicoreria.licoreria.model.ClienteWeb;
import com.tulicoreria.licoreria.model.DireccionEnvio;

public interface ClienteWebService {

    ClienteWeb registrar(RegistroClienteWebDTO dto);

    ClienteWeb findByEmail(String email);

    void agregarDireccion(String email, DireccionEnvio direccion);

    void eliminarDireccion(String email, Long direccionId);

    void marcarDefaultDireccion(String email, Long direccionId);

    void actualizarPerfil(String email, String nombre, String apellido, String telefono);
}
