package com.tulicoreria.licoreria.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.math.BigDecimal;

/**
 * Expone en el Model, para toda vista renderizada por un @Controller, los
 * valores de configuración que usa el widget de chatbot embebido en
 * publica/layout.html (Yape, WhatsApp, pedido mínimo), sin tener que
 * repetirlos en cada controlador.
 */
@ControllerAdvice(annotations = Controller.class)
public class ChatbotConfigAdvice {

    @Value("${app.pago.yape.numero:51977968942}")
    private String yapeNumero;

    @Value("${app.pago.yape.nombre:Tu Licorería}")
    private String yapeNombre;

    @Value("${app.carrito.pedido-minimo:50.00}")
    private BigDecimal pedidoMinimo;

    @Value("${app.whatsapp.numero:51977968942}")
    private String whatsappNumero;

    @ModelAttribute
    public void agregarConfigChatbot(Model model) {
        model.addAttribute("chatbotYapeNumero", yapeNumero);
        model.addAttribute("chatbotYapeNombre", yapeNombre);
        model.addAttribute("chatbotPedidoMinimo", pedidoMinimo);
        model.addAttribute("chatbotWhatsappNumero", whatsappNumero);
    }
}
