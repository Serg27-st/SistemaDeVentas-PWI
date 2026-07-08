package com.tulicoreria.licoreria.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tulicoreria.licoreria.dto.UsuarioRequestDTO;
import com.tulicoreria.licoreria.repository.RolRepository;
import com.tulicoreria.licoreria.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
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
    public String crear(@ModelAttribute("usuario") UsuarioRequestDTO dto,
            Model model,
            RedirectAttributes flash) {
        try {
            usuarioService.crear(dto);
            flash.addFlashAttribute("exito", "Usuario creado correctamente");
            return "redirect:/usuarios"; // Solo redirecciona si todo salió bien
        } catch (RuntimeException e) {
            // 💡 MEJORA: Si hay error, recarga la misma página inyectando los datos enviados 
            // para que el administrador no pierda el texto que ya digitó.
            model.addAttribute("error", e.getMessage());
            model.addAttribute("roles", rolRepository.findAll());
            return "usuarios/formulario";
        }
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
