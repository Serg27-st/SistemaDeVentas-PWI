package com.tulicoreria.licoreria.controller;

import com.tulicoreria.licoreria.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ReporteService reporteService;

    @GetMapping("/")
    public String index() {
        return "redirect:/inicio";
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

    // GET y POST: Spring reenvía aquí (conservando el método original) cuando
    // Security deniega el acceso a un POST — sin el POST explícito, ese
    // reenvío devuelve 405 en vez de la página de error.
    @RequestMapping(value = "/error/403", method = {RequestMethod.GET, RequestMethod.POST})
    public String accesoDenegado() {
        return "error/403";
    }
}
