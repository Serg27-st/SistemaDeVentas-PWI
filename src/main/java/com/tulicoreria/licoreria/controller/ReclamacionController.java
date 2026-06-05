package com.tulicoreria.licoreria.controller;

import com.tulicoreria.licoreria.dto.ReclamacionRequestDTO;
import com.tulicoreria.licoreria.service.CategoriaService;
import com.tulicoreria.licoreria.service.ReclamacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reclamaciones")
@RequiredArgsConstructor
public class ReclamacionController {

    private final ReclamacionService reclamacionService;
    private final CategoriaService categoriaService;

    /** Categorías disponibles para el navbar del layout público */
    @ModelAttribute("categorias")
    public java.util.List<?> categorias() {
        return categoriaService.listarActivas();
    }

    // ── Formulario público ────────────────────────────────────────────────────

    @GetMapping
    public String formulario(Model model) {
        model.addAttribute("reclamacionRequest", new ReclamacionRequestDTO());
        return "publica/reclamaciones";
    }

    @PostMapping
    public String registrar(@ModelAttribute ReclamacionRequestDTO dto,
                            RedirectAttributes flash) {
        try {
            var r = reclamacionService.registrar(dto);
            flash.addFlashAttribute("exitoReclamo",
                "Tu " + dto.getTipoReclamo().toLowerCase()
                    + " fue registrada correctamente. Número de referencia: <strong>#" + r.getId() + "</strong>");
        } catch (Exception e) {
            flash.addFlashAttribute("errorReclamo", e.getMessage());
        }
        return "redirect:/reclamaciones";
    }

    // ── Panel admin ───────────────────────────────────────────────────────────

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String listar(Model model) {
        model.addAttribute("reclamaciones", reclamacionService.listarTodas());
        model.addAttribute("pendientes", reclamacionService.contarPendientes());
        return "reclamaciones/lista";
    }

    @PostMapping("/{id}/atender")
    @PreAuthorize("hasRole('ADMIN')")
    public String atender(@PathVariable Long id, RedirectAttributes flash) {
        try {
            reclamacionService.atender(id);
            flash.addFlashAttribute("exito", "Reclamación #" + id + " marcada como atendida.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reclamaciones/admin";
    }
}
