package com.tulicoreria.licoreria.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
public class ReclamacionResponseDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String tipoReclamo;
    private String descripcion;
    private String productoAfectado;
    private LocalDateTime fechaRegistro;
    private String estado;
}