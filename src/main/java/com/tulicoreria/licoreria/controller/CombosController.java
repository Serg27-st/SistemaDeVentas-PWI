package com.tulicoreria.licoreria.controller;

import com.tulicoreria.licoreria.model.Promocion;
import com.tulicoreria.licoreria.service.CategoriaService;
import com.tulicoreria.licoreria.service.PromocionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        // Pre-computar totalNormal por combo para evitar lambdas Java en SpEL del template
        Map<Long, BigDecimal> totalesNormales = new LinkedHashMap<>();
        for (Promocion combo : combos) {
            BigDecimal total = combo.getItems().stream()
                    .filter(ci -> ci.getProducto() != null)
                    .map(ci -> ci.getProducto().getPrecioVenta()
                            .multiply(BigDecimal.valueOf(ci.getCantidad())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalesNormales.put(combo.getId(), total);
        }
        model.addAttribute("totalesNormales", totalesNormales);

        return "publica/combos";
    }
}
