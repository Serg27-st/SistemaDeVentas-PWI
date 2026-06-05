package com.tulicoreria.licoreria.controller;

import com.tulicoreria.licoreria.model.Promocion;
import com.tulicoreria.licoreria.service.CategoriaService;
import com.tulicoreria.licoreria.service.PromocionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CombosController {

    private final PromocionService  promocionService;
    private final CategoriaService  categoriaService;

    @ModelAttribute
    public void commonAttributes(Model model) {
        model.addAttribute("categorias", categoriaService.listarActivas());
    }

    @GetMapping("/combos")
    public String combos(Model model) {
        List<Promocion> combos = promocionService.findCombosActivos();
        model.addAttribute("combos", combos);
        return "publica/combos";
    }
}
