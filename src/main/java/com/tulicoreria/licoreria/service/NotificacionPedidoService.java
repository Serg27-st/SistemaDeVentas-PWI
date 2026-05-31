package com.tulicoreria.licoreria.service;

import com.tulicoreria.licoreria.model.Pedido;
import com.tulicoreria.licoreria.model.PedidoItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Envía correos transaccionales de pedidos.
 * Solo actúa si {@code app.mail.enabled=true}.
 * JavaMailSender es OPCIONAL: si no está configurado el servidor de correo
 * (mail.enabled=false), el bean simplemente no se inyecta y se omite el envío.
 */
@Service
public class NotificacionPedidoService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionPedidoService.class);

    /**
     * Inyección opcional: si spring.mail.* no está configurado,
     * Spring Boot no crea el bean y mailSender queda null.
     * El guard {@code if (!mailEnabled || mailSender == null)} evita NPE.
     */
    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.admin:noreply@tulicoreria.com}")
    private String remitente;

    /** Correo de confirmación cuando el pago es exitoso o es contra entrega. */
    public void enviarConfirmacion(Pedido pedido) {
        if (!mailEnabled || mailSender == null || pedido.getEmailCliente() == null) return;
        try {
            var msg = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(pedido.getEmailCliente());
            helper.setSubject("✅ Tu pedido " + pedido.getNumeroPedido() + " fue confirmado — Tu Licorería");
            helper.setText(buildHtmlConfirmacion(pedido), true);
            mailSender.send(msg);
            log.info("Correo de confirmación enviado a {}", pedido.getEmailCliente());
        } catch (Exception e) {
            log.warn("No se pudo enviar correo de confirmación: {}", e.getMessage());
        }
    }

    /** Correo cuando el pago falla. */
    public void enviarErrorPago(Pedido pedido, String motivo) {
        if (!mailEnabled || mailSender == null || pedido.getEmailCliente() == null) return;
        try {
            var msg = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(pedido.getEmailCliente());
            helper.setSubject("⚠️ No pudimos procesar tu pago — Tu Licorería");
            helper.setText(buildHtmlError(pedido, motivo), true);
            mailSender.send(msg);
        } catch (Exception e) {
            log.warn("No se pudo enviar correo de error: {}", e.getMessage());
        }
    }

    // ── Helpers de HTML ────────────────────────────────────────────────────

    private String buildHtmlConfirmacion(Pedido pedido) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='font-family:DM Sans,Arial,sans-serif;max-width:600px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;'>");
        sb.append("<div style='background:linear-gradient(135deg,#0d0d16,#1a1025);padding:2rem;text-align:center;'>");
        sb.append("<h1 style='color:#c9a227;font-family:serif;margin:0;'>TU LICORERÍA</h1>");
        sb.append("<p style='color:rgba(255,255,255,.7);margin:.5rem 0 0;font-size:.9rem;'>Panel de pedidos</p>");
        sb.append("</div>");

        sb.append("<div style='padding:2rem;'>");
        sb.append("<h2 style='color:#1a1a2e;margin-top:0;'>¡Gracias por tu compra, ")
          .append(pedido.getNombreCliente() != null ? pedido.getNombreCliente() : "cliente")
          .append("! 🎉</h2>");

        String estadoTexto = pedido.getEstado() == Pedido.EstadoPedido.PAGADO
                ? "Pago confirmado — las botellas van en camino"
                : "Pedido registrado — pagarás al recibir";
        sb.append("<p style='color:#374151;'>").append(estadoTexto).append("</p>");

        sb.append("<div style='background:#f9f9fb;border-radius:10px;padding:1.5rem;margin:1.5rem 0;'>");
        sb.append("<b style='color:#6b7280;font-size:.8rem;text-transform:uppercase;letter-spacing:1px;'>N° de pedido</b><br>");
        sb.append("<span style='font-size:1.3rem;font-weight:700;color:#c9a227;letter-spacing:2px;'>")
          .append(pedido.getNumeroPedido()).append("</span>");
        sb.append("</div>");

        // Items
        sb.append("<table style='width:100%;border-collapse:collapse;margin-bottom:1rem;'>");
        sb.append("<tr style='background:#f3f4f6;'><th style='text-align:left;padding:.5rem;font-size:.8rem;'>Producto</th><th style='text-align:center;padding:.5rem;font-size:.8rem;'>Cant.</th><th style='text-align:right;padding:.5rem;font-size:.8rem;'>Subtotal</th></tr>");
        for (PedidoItem item : pedido.getItems()) {
            sb.append("<tr style='border-bottom:1px solid #f0f0f0;'>");
            sb.append("<td style='padding:.5rem;font-size:.87rem;'>").append(item.getProductoNombre()).append("</td>");
            sb.append("<td style='text-align:center;padding:.5rem;font-size:.87rem;'>").append(item.getCantidad()).append("</td>");
            sb.append("<td style='text-align:right;padding:.5rem;font-size:.87rem;'>S/. ")
              .append(String.format("%.2f", item.getSubtotal())).append("</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");

        // Totales
        sb.append("<div style='text-align:right;'>");
        sb.append("<div style='color:#6b7280;font-size:.85rem;'>Subtotal: S/. ").append(fmt(pedido.getSubtotal())).append("</div>");
        sb.append("<div style='color:#6b7280;font-size:.85rem;'>IGV (18%): S/. ").append(fmt(pedido.getIgv())).append("</div>");
        if (pedido.getCostoEnvio() != null && pedido.getCostoEnvio().compareTo(BigDecimal.ZERO) > 0) {
            sb.append("<div style='color:#6b7280;font-size:.85rem;'>Envío (").append(pedido.getDistrito()).append("): S/. ").append(fmt(pedido.getCostoEnvio())).append("</div>");
        }
        sb.append("<div style='font-size:1.1rem;font-weight:700;color:#c9a227;margin-top:.5rem;'>TOTAL: S/. ").append(fmt(pedido.getTotal())).append("</div>");
        sb.append("</div>");

        if (pedido.getDistrito() != null) {
            sb.append("<p style='margin-top:1.5rem;color:#374151;font-size:.87rem;'>📍 <b>Entrega en:</b> ").append(pedido.getDireccion())
              .append(", ").append(pedido.getDistrito()).append("</p>");
        }

        sb.append("<div style='background:rgba(201,162,39,.08);border:1px solid rgba(201,162,39,.3);border-radius:8px;padding:1rem;margin-top:1.5rem;font-size:.85rem;color:#374151;'>");
        sb.append("⏰ Un representante de Tu Licorería se contactará contigo para coordinar la entrega. ");
        sb.append("Tiempo estimado: <b>30–60 minutos</b>.");
        sb.append("</div>");

        sb.append("</div>");
        sb.append("<div style='background:#f9f9fb;padding:1rem;text-align:center;font-size:.75rem;color:#9ca3af;'>");
        sb.append("🍾 Gracias por elegirnos. Recuerda: venta exclusiva para mayores de 18 años.");
        sb.append("</div>");
        sb.append("</div>");
        return sb.toString();
    }

    private String buildHtmlError(Pedido pedido, String motivo) {
        return "<div style='font-family:Arial,sans-serif;max-width:560px;margin:0 auto;padding:2rem;'>" +
               "<h2 style='color:#b91c1c;'>⚠️ No pudimos procesar tu pago</h2>" +
               "<p>Hola " + (pedido.getNombreCliente() != null ? pedido.getNombreCliente() : "") + ",</p>" +
               "<p>Tu pedido <b>" + pedido.getNumeroPedido() + "</b> no pudo completarse.</p>" +
               "<p style='background:#fef2f2;border:1px solid #fecaca;padding:1rem;border-radius:8px;'>" +
               "<b>Motivo:</b> " + motivo + "</p>" +
               "<p>Puedes intentar de nuevo con otra tarjeta o elegir pago contra entrega.</p>" +
               "</div>";
    }

    private static String fmt(BigDecimal v) {
        return v != null ? String.format("%.2f", v) : "0.00";
    }
}
