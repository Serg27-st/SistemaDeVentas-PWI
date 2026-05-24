package com.tulicoreria.licoreria.controller;

import com.tulicoreria.licoreria.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ReporteService reporteService;

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("dashboard", reporteService.dashboard());
        return "dashboard/index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/inicio")
    public String inicio(Model model) {
        // Para visualizar la portada, lo mínimo requerido por la vista son estas variables.
        // Si no hay datos aún, la vista igualmente renderiza (con listas vacías).
        model.addAttribute("categorias", java.util.List.of());
        model.addAttribute("destacados", java.util.List.of());
        return "publica/inicio";
    }

    @GetMapping("/catalogo")
    public String catalogo(Model model) {
        // Endpoint mínimo para que los links de la portada no fallen.
        model.addAttribute("productos", java.util.List.of());
        model.addAttribute("categorias", java.util.List.of());
        return "productos/lista";
    }

    @GetMapping("/error/403")
    public String accesoDenegado() {
        return "error/403";
    }
}
