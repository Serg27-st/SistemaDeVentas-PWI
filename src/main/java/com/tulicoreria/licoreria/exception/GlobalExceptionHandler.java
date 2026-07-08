package com.tulicoreria.licoreria.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Red de seguridad para excepciones de negocio que ningún controlador atrapó
 * localmente (p. ej. un endpoint de solo lectura con un id inexistente en la URL).
 * No reemplaza los try/catch que ya existen en los controladores: Spring solo
 * invoca este advice cuando la excepción sale del método sin haber sido
 * capturada antes, así que los flujos de crear/editar (que necesitan reusar
 * el formulario con los datos ya escritos) siguen funcionando igual.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({RecursoNoEncontradoException.class, ReglaDeNegocioException.class})
    public String manejarExcepcionDeNegocio(RuntimeException ex, HttpServletRequest request, RedirectAttributes flash) {
        flash.addFlashAttribute("error", ex.getMessage());
        flash.addFlashAttribute("errorMensaje", ex.getMessage());
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/inicio");
    }
}
