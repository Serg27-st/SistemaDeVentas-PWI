package com.tulicoreria.licoreria.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tarifas de envío por distrito de Lima y Callao.
 * Actualizar las tarifas aquí cuando cambien los precios de delivery.
 */
@Service
public class EnvioService {

    public static final String RECOJO_TIENDA = "Recojo en tienda";
    public static final BigDecimal COSTO_RECOJO = BigDecimal.ZERO;

    /** Mapa ordenado: nombre del distrito → costo en soles. */
    private static final Map<String, BigDecimal> TARIFAS;

    static {
        TARIFAS = new LinkedHashMap<>();
        TARIFAS.put(RECOJO_TIENDA,          new BigDecimal("0.00"));
        // Zona 1 — Cercano
        TARIFAS.put("Miraflores",           new BigDecimal("5.00"));
        TARIFAS.put("San Isidro",           new BigDecimal("5.00"));
        TARIFAS.put("Barranco",             new BigDecimal("5.00"));
        TARIFAS.put("Surquillo",            new BigDecimal("5.00"));
        TARIFAS.put("San Borja",            new BigDecimal("5.00"));
        TARIFAS.put("Lince",                new BigDecimal("5.00"));
        TARIFAS.put("Magdalena del Mar",    new BigDecimal("5.00"));
        // Zona 2 — Media
        TARIFAS.put("Surco",                new BigDecimal("7.00"));
        TARIFAS.put("La Molina",            new BigDecimal("7.00"));
        TARIFAS.put("Chorrillos",           new BigDecimal("7.00"));
        TARIFAS.put("San Miguel",           new BigDecimal("7.00"));
        TARIFAS.put("Pueblo Libre",         new BigDecimal("7.00"));
        TARIFAS.put("Jesús María",          new BigDecimal("7.00"));
        TARIFAS.put("Breña",                new BigDecimal("7.00"));
        TARIFAS.put("La Victoria",          new BigDecimal("7.00"));
        TARIFAS.put("Cercado de Lima",      new BigDecimal("7.00"));
        TARIFAS.put("Rímac",                new BigDecimal("7.00"));
        // Zona 3 — Lejano
        TARIFAS.put("Callao",               new BigDecimal("10.00"));
        TARIFAS.put("Los Olivos",           new BigDecimal("10.00"));
        TARIFAS.put("San Martín de Porres", new BigDecimal("10.00"));
        TARIFAS.put("Comas",                new BigDecimal("10.00"));
        TARIFAS.put("Independencia",        new BigDecimal("10.00"));
        TARIFAS.put("Ate",                  new BigDecimal("10.00"));
        TARIFAS.put("San Juan de Lurigancho", new BigDecimal("10.00"));
        TARIFAS.put("Villa El Salvador",    new BigDecimal("10.00"));
        TARIFAS.put("Villa María del Triunfo", new BigDecimal("10.00"));
        TARIFAS.put("Lurín",                new BigDecimal("10.00"));
        TARIFAS.put("Pachacámac",           new BigDecimal("10.00"));
        TARIFAS.put("Puente Piedra",        new BigDecimal("10.00"));
        TARIFAS.put("Carabayllo",           new BigDecimal("10.00"));
        // Otras provincias
        TARIFAS.put("Otras provincias",     new BigDecimal("20.00"));
    }

    public Map<String, BigDecimal> getTarifas() {
        return TARIFAS;
    }

    public BigDecimal getCosto(String distrito) {
        if (distrito == null || distrito.isBlank()) return BigDecimal.TEN;
        return TARIFAS.getOrDefault(distrito, new BigDecimal("10.00"));
    }
}
