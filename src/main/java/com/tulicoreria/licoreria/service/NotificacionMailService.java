package com.tulicoreria.licoreria.service;

import com.tulicoreria.licoreria.model.Reclamacion;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Servicio de alertas por correo para reclamaciones.
 * Solo se activa cuando app.mail.enabled=true en application.properties.
 */
@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
@RequiredArgsConstructor
public class NotificacionMailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.admin:admin@licoreria.com}")
    private String emailAdmin;

    @Async
    public void enviarAlertaReclamacion(Reclamacion r) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(emailAdmin);
            msg.setSubject("[ALERTA] Nueva " + r.getTipoReclamo().name()
                    + " en Libro de Reclamaciones — #" + r.getId());
            msg.setText(
                "Se registró una nueva " + r.getTipoReclamo().name()
                    + " en el libro de reclamaciones.\n\n"
                + "═══════════════════════════════════\n"
                + " DATOS DEL CLIENTE\n"
                + "═══════════════════════════════════\n"
                + "Nombre  : " + r.getNombre() + " " + r.getApellido() + "\n"
                + "Email   : " + r.getEmail() + "\n"
                + "Teléfono: " + (r.getTelefono() != null ? r.getTelefono() : "—") + "\n\n"
                + "═══════════════════════════════════\n"
                + " DETALLE DEL " + r.getTipoReclamo().name() + "\n"
                + "═══════════════════════════════════\n"
                + "Producto afectado: " + (r.getProductoAfectado() != null ? r.getProductoAfectado() : "—") + "\n"
                + "Descripción:\n" + r.getDescripcion() + "\n\n"
                + "Fecha: " + r.getFechaRegistro() + "\n\n"
                + "Gestiona este caso en el panel admin:\n"
                + "http://localhost:8081/reclamaciones/admin\n"
            );
            mailSender.send(msg);
        } catch (Exception e) {
            // No relanzamos — el reclamo queda guardado aunque falle el correo
            System.err.println("⚠ No se pudo enviar el correo de alerta: " + e.getMessage());
        }
    }
}
