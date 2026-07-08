package com.tulicoreria.licoreria.controller;

import com.tulicoreria.licoreria.dto.ItemCarritoDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Acceso al carrito y combos guardados en la sesión HTTP.
 * Compartido por los controladores públicos que leen/escriben el carrito
 * (PublicController, CheckoutController, CarritoApiController) para evitar
 * repetir la misma lectura/inicialización de atributos de sesión en cada uno.
 */
@Component
public class CarritoSessionHelper {

    private static final String CARRITO_KEY = "carrito";
    private static final String COMBOS_KEY  = "combosCarrito";

    @SuppressWarnings("unchecked")
    public Map<Long, ItemCarritoDTO> getCarrito(HttpSession session) {
        Map<Long, ItemCarritoDTO> c = (Map<Long, ItemCarritoDTO>) session.getAttribute(CARRITO_KEY);
        if (c == null) {
            c = new LinkedHashMap<>();
            session.setAttribute(CARRITO_KEY, c);
        }
        return c;
    }

    @SuppressWarnings("unchecked")
    public Map<Long, Integer> getCombosCarrito(HttpSession session) {
        Map<Long, Integer> c = (Map<Long, Integer>) session.getAttribute(COMBOS_KEY);
        if (c == null) {
            c = new LinkedHashMap<>();
            session.setAttribute(COMBOS_KEY, c);
        }
        return c;
    }

    public void limpiar(HttpSession session) {
        session.removeAttribute(CARRITO_KEY);
        session.removeAttribute(COMBOS_KEY);
    }

    public static ItemCarritoDTO copiarItem(ItemCarritoDTO orig) {
        return ItemCarritoDTO.builder()
                .productoId(orig.getProductoId())
                .nombre(orig.getNombre())
                .marca(orig.getMarca())
                .urlImagen(orig.getUrlImagen())
                .precioUnitario(orig.getPrecioUnitario())
                .cantidad(orig.getCantidad())
                .build();
    }
}
