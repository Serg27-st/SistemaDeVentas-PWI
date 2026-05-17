package com.tulicoreria.licoreria.controller;

import com.tulicoreria.licoreria.dto.UsuarioRequestDTO;
import com.tulicoreria.licoreria.repository.RolRepository;
import com.tulicoreria.licoreria.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final RolRepository rolRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        model.addAttribute("usuario", new UsuarioRequestDTO());
        model.addAttribute("roles", rolRepository.findAll());
        return "usuarios/formulario";
    }

    @PostMapping("/nuevo")
    public String crear(@ModelAttribute UsuarioRequestDTO dto,
                        RedirectAttributes flash) {
        try {
            usuarioService.crear(dto);
            flash.addFlashAttribute("exito", "Usuario creado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            usuarioService.desactivar(id);
            flash.addFlashAttribute("exito", "Usuario desactivado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/usuarios";
    }
}
