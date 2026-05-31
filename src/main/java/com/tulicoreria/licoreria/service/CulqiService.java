package com.tulicoreria.licoreria.service;

/**
 * Integración con la pasarela Culqi.
 * El frontend tokeniza la tarjeta con checkout.js v4 y nos envía el token.
 * Este servicio crea el cargo en la API de Culqi usando la clave secreta.
 */
public interface CulqiService {

    /**
     * Cobra un monto en la tarjeta asociada al token.
     *
     * @param token         token generado por Culqi JS en el frontend
     * @param montoSoles    monto en soles (e.g. 110.50)
     * @param email         correo del cliente
     * @param descripcion   descripción del cargo
     * @return              ID del cargo en Culqi (e.g. chr_live_...)
     * @throws RuntimeException si el cobro es rechazado o la API devuelve error
     */
    String cobrar(String token, java.math.BigDecimal montoSoles, String email, String descripcion);
}
