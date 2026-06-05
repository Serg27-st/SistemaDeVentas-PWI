package com.tulicoreria.licoreria.dto;

import lombok.Data;

@Data
public class ReclamacionRequestDTO {

    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    /** "QUEJA" o "RECLAMO" */
    private String tipoReclamo;
    private String descripcion;
    private String productoAfectado;
}
