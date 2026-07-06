package com.tulicoreria.licoreria.security;

import java.io.IOException;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Fuerza la resolución del CsrfToken diferido (DeferredCsrfToken) antes de
 * que Thymeleaf empiece a renderizar. Sin esto, la primera lectura de
 * ${_csrf.token} ocurre a mitad del streaming del HTML, cuando la respuesta
 * ya está comprometida, y la creación de sesión/cookie en ese punto rompe
 * el render (respuesta truncada en las vistas públicas).
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        // Sólo forzamos la resolución en GET: es lo único que renderiza
        // Thymeleaf directamente. En POST (p.ej. /login) forzar la
        // resolución aquí interfiere con el AuthenticationFailureHandler
        // y provoca un 500 en vez del redirect a /login?error=true.
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (csrfToken != null) {
                csrfToken.getToken();
            }
        }
        filterChain.doFilter(request, response);
    }
}
