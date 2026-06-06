package com.tulicoreria.licoreria.controller;

import com.tulicoreria.licoreria.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@RequiredArgsConstructor
public class InfoController {

    private final CategoriaService categoriaService;

    @ModelAttribute
    public void commonAttributes(Model model) {
        model.addAttribute("categorias", categoriaService.listarActivas());
    }

    @GetMapping("/nosotros")
    public String nosotros() {
        return "publica/info/nosotros";
    }

    @GetMapping("/privacidad")
    public String privacidad() {
        return "publica/info/privacidad";
    }

    @GetMapping("/terminos")
    public String terminos() {
        return "publica/info/terminos";
    }

    @GetMapping("/faq")
    public String faq() {
        return "publica/info/faq";
    }
}
