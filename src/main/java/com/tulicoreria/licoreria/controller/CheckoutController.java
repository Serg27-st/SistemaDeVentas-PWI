package com.tulicoreria.licoreria.controller;

import com.tulicoreria.licoreria.dto.DatosEnvioDTO;
import com.tulicoreria.licoreria.dto.ItemCarritoDTO;
import com.tulicoreria.licoreria.model.ClienteWeb;
import com.tulicoreria.licoreria.model.Pedido;
import com.tulicoreria.licoreria.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Flujo de checkout en 4 pantallas:
 *  1. /checkout/iniciar            — validar carrito y crear Pedido
 *  2. /checkout/acceso             — login / registro / continuar como invitado
 *  3. /checkout/{id}/datos         — formulario de envío (Pantalla 2)
 *  4. /checkout/{id}/pago          — pasarela (Pantalla 3)
 *  5. /checkout/{id}/ok            — confirmación (Pantalla 4)
 *  6. /checkout/{id}/error         — fallo de pago (Pantalla 4)
 */
@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final PedidoService      pedidoService;
    private final PromocionService   promocionService;
    private final ClienteWebService  clienteWebService;
    private final EnvioService       envioService;
    private final CategoriaService   categoriaService;

    private static final String CARRITO_KEY = "carrito";
    private static final String COMBOS_KEY  = "combosCarrito";
    private static final String GUEST_EMAIL = "checkout_guest_email";
    private static final BigDecimal IGV_RATE = new BigDecimal("0.18");

    // ── Datos Yape configurables desde application.properties ────────────────
    @Value("${app.pago.yape.numero:51977968942}")
    private String yapeNumero;

    @Value("${app.pago.yape.nombre:Tu Licorería}")
    private String yapeNombre;

    /** Inyectar categorías en todos los modelos (necesario para el navbar). */
    @ModelAttribute
    public void commonAttributes(Model model) {
        model.addAttribute("categorias", categoriaService.listarActivas());
    }

    // ────────────────────────────────────────────────────────────────────────
    // PASO 0 — Iniciar checkout desde el carrito
    // ────────────────────────────────────────────────────────────────────────

    @PostMapping("/iniciar")
    public String iniciar(HttpSession session, Authentication auth, RedirectAttributes flash) {
        Map<Long, ItemCarritoDTO> carrito = getCarrito(session);
        Map<Long, Integer>        combos  = getCombosCarrito(session);

        if (carrito.isEmpty() && combos.isEmpty()) {
            flash.addFlashAttribute("errorMensaje", "Tu carrito está vacío.");
            return "redirect:/carrito";
        }

        // Construir lista con descuentos de volumen aplicados
        List<ItemCarritoDTO> items = carrito.values().stream().map(orig -> {
            ItemCarritoDTO copia = copiarItem(orig);
            promocionService.aplicarDescuentoVolumen(copia);
            return copia;
        }).collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        // Si el usuario no está autenticado como CLIENTE → pantalla de acceso
        if (!esCliente(auth)) {
            // Guardar items en sesión para después de iniciar sesión
            session.setAttribute("checkout_items_pendientes", Boolean.TRUE);
            return "redirect:/checkout/acceso";
        }

        // Crear pedido
        try {
            Long clienteWebId = resolverClienteWebId(auth);
            Pedido pedido = pedidoService.crear(items, combos, clienteWebId, null);
            return "redirect:/checkout/" + pedido.getId() + "/datos";
        } catch (RuntimeException e) {
            flash.addFlashAttribute("errorMensaje", e.getMessage());
            return "redirect:/carrito";
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // PANTALLA DE ACCESO — Login / Registro / Invitado
    // ────────────────────────────────────────────────────────────────────────

    @GetMapping("/acceso")
    public String acceso(Model model, Authentication auth) {
        if (esCliente(auth)) return "redirect:/checkout/iniciar-autenticado";
        return "publica/checkout/acceso";
    }

    /** Tras login exitoso el CustomAuthSuccessHandler redirige aquí si venía del checkout. */
    @GetMapping("/iniciar-autenticado")
    public String iniciarAutenticado(HttpSession session, Authentication auth, RedirectAttributes flash) {
        return iniciar(session, auth, flash);
    }

    /** El cliente elige continuar como invitado → sólo necesita su correo. */
    @PostMapping("/acceso/invitado")
    public String continuarInvitado(@RequestParam String email,
                                    HttpSession session,
                                    RedirectAttributes flash) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            flash.addFlashAttribute("errorAcceso", "Ingresa un correo válido.");
            return "redirect:/checkout/acceso";
        }
        session.setAttribute(GUEST_EMAIL, email.trim().toLowerCase());

        Map<Long, ItemCarritoDTO> carrito = getCarrito(session);
        Map<Long, Integer>        combos  = getCombosCarrito(session);

        List<ItemCarritoDTO> items = carrito.values().stream().map(orig -> {
            ItemCarritoDTO copia = copiarItem(orig);
            promocionService.aplicarDescuentoVolumen(copia);
            return copia;
        }).collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        try {
            Pedido pedido = pedidoService.crear(items, combos, null, email.trim().toLowerCase());
            return "redirect:/checkout/" + pedido.getId() + "/datos";
        } catch (RuntimeException e) {
            flash.addFlashAttribute("errorMensaje", e.getMessage());
            return "redirect:/carrito";
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // PANTALLA 2 — Datos del cliente y envío
    // ────────────────────────────────────────────────────────────────────────

    @GetMapping("/{id}/datos")
    public String datosForm(@PathVariable Long id, Model model, Authentication auth,
                            HttpSession session) {
        Pedido pedido = safe(id, model);
        if (pedido == null) return "redirect:/carrito";

        DatosEnvioDTO dto = new DatosEnvioDTO();
        dto.setTipoComprobante("BOLETA");

        // Pre-fill si está autenticado
        if (esCliente(auth)) {
            try {
                ClienteWeb cw = clienteWebService.findByEmail(auth.getName());
                dto.setNombre(cw.getNombre());
                dto.setApellido(cw.getApellido());
                dto.setEmail(cw.getEmail());
                dto.setTelefono(cw.getTelefono());
                // Pre-fill DNI si ya está registrado (no es el placeholder WEB-...)
                if (cw.getCliente() != null) {
                    String numDoc = cw.getCliente().getNumeroDocumento();
                    if (numDoc != null && !numDoc.startsWith("WEB-")) {
                        dto.setDni(numDoc);
                    }
                }
                // Pre-fill dirección por defecto si tiene
                cw.getDirecciones().stream()
                  .filter(d -> d.isEsDefault())
                  .findFirst()
                  .ifPresent(dir -> {
                      dto.setDireccion(dir.getDireccionCompleta());
                      dto.setReferencia(dir.getReferencia());
                      dto.setDistrito(dir.getDistrito());
                  });
            } catch (Exception ignored) {}
        } else {
            // Invitado: pre-fill email
            String guestEmail = (String) session.getAttribute(GUEST_EMAIL);
            if (guestEmail != null) dto.setEmail(guestEmail);
        }

        model.addAttribute("dto", dto);
        model.addAttribute("pedido", pedido);
        model.addAttribute("tarifasEnvio", envioService.getTarifas());
        return "publica/checkout/datos";
    }

    @PostMapping("/{id}/datos")
    public String datosSubmit(@PathVariable Long id,
                              @ModelAttribute DatosEnvioDTO dto,
                              RedirectAttributes flash) {
        try {
            pedidoService.actualizarDatosEnvio(id, dto);
            return "redirect:/checkout/" + id + "/pago";
        } catch (RuntimeException e) {
            flash.addFlashAttribute("errorMensaje", e.getMessage());
            return "redirect:/checkout/" + id + "/datos";
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // PANTALLA 3 — Efectuar pago
    // ────────────────────────────────────────────────────────────────────────

    @GetMapping("/{id}/pago")
    public String pagoForm(@PathVariable Long id, Model model) {
        Pedido pedido = safe(id, model);
        if (pedido == null) return "redirect:/carrito";

        // Formatear número Yape para mostrar: +51 977 968 942
        String yapeFormateado = formatearYape(yapeNumero);

        model.addAttribute("pedido",      pedido);
        model.addAttribute("yapeNumero",  yapeFormateado);
        model.addAttribute("yapeNombre",  yapeNombre);
        return "publica/checkout/pago";
    }

    /** Procesar pago con Yape o Efectivo (contra entrega). */
    @PostMapping("/{id}/pagar/entrega")
    public String pagarEntrega(@PathVariable Long id,
                               @RequestParam(defaultValue = "CONTRA_ENTREGA") String metodoPago,
                               RedirectAttributes flash,
                               HttpSession session) {
        try {
            pedidoService.confirmarContraEntrega(id, metodoPago);
            // Limpiar carrito de la sesión
            session.removeAttribute(CARRITO_KEY);
            session.removeAttribute(COMBOS_KEY);
            session.removeAttribute(GUEST_EMAIL);
            return "redirect:/checkout/" + id + "/ok";
        } catch (RuntimeException e) {
            flash.addFlashAttribute("errorPago", e.getMessage());
            return "redirect:/checkout/" + id + "/error";
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // PANTALLA 4 — Confirmación / Error
    // ────────────────────────────────────────────────────────────────────────

    @GetMapping("/{id}/ok")
    public String confirmacion(@PathVariable Long id, Model model,
                               HttpSession session) {
        Pedido pedido = safe(id, model);
        if (pedido == null) return "redirect:/carrito";

        // Limpiar carrito si aún no se limpió
        session.removeAttribute(CARRITO_KEY);
        session.removeAttribute(COMBOS_KEY);
        session.removeAttribute(GUEST_EMAIL);

        model.addAttribute("pedido", pedido);
        return "publica/checkout/confirmacion";
    }

    @GetMapping("/{id}/error")
    public String errorPago(@PathVariable Long id, Model model) {
        Pedido pedido = safe(id, model);
        if (pedido == null) return "redirect:/carrito";
        model.addAttribute("pedido", pedido);
        return "publica/checkout/error";
    }

    /** Formatea número de 11 dígitos (51XXXXXXXXX) como +51 XXX XXX XXX */
    private String formatearYape(String numero) {
        if (numero == null) return "";
        String limpio = numero.replaceAll("[^0-9]", "");
        if (limpio.startsWith("51") && limpio.length() == 11) {
            String local = limpio.substring(2); // 9 dígitos
            return "+51 " + local.substring(0,3) + " " + local.substring(3,6) + " " + local.substring(6);
        }
        return "+" + limpio;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Pedido safe(Long id, Model model) {
        try {
            return pedidoService.buscarPorId(id);
        } catch (RuntimeException e) {
            model.addAttribute("errorMensaje", e.getMessage());
            return null;
        }
    }

    private boolean esCliente(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));
    }

    private Long resolverClienteWebId(Authentication auth) {
        if (auth == null) return null;
        try {
            ClienteWeb cw = clienteWebService.findByEmail(auth.getName());
            return cw.getId();
        } catch (Exception e) {
            return null;
        }
    }

    private static ItemCarritoDTO copiarItem(ItemCarritoDTO orig) {
        return ItemCarritoDTO.builder()
                .productoId(orig.getProductoId()).nombre(orig.getNombre())
                .marca(orig.getMarca()).urlImagen(orig.getUrlImagen())
                .precioUnitario(orig.getPrecioUnitario()).cantidad(orig.getCantidad())
                .build();
    }

    @SuppressWarnings("unchecked")
    private Map<Long, ItemCarritoDTO> getCarrito(HttpSession session) {
        Map<Long, ItemCarritoDTO> c = (Map<Long, ItemCarritoDTO>) session.getAttribute(CARRITO_KEY);
        return c != null ? c : new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> getCombosCarrito(HttpSession session) {
        Map<Long, Integer> c = (Map<Long, Integer>) session.getAttribute(COMBOS_KEY);
        return c != null ? c : new LinkedHashMap<>();
    }
}
