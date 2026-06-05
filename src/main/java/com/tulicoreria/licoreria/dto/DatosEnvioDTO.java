package com.tulicoreria.licoreria.dto;

import lombok.*;

/**
 * Formulario de la Pantalla 2: datos del cliente y dirección de entrega.
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatosEnvioDTO {

    // ── Identificación ─────────────────────────────────────────────────────
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String dni;

    // ── Dirección de entrega ───────────────────────────────────────────────
    private String direccion;      // Calle y número
    private String departamento;   // Piso, dpto, interior (opcional)
    private String referencia;     // Referencia para el repartidor
    private String distrito;       // Selector desplegable

    // ── Comprobante ────────────────────────────────────────────────────────
    private String tipoComprobante; // BOLETA | FACTURA
    private String ruc;
    private String razonSocial;
}
