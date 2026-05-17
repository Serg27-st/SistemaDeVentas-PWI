package com.tulicoreria.licoreria.controller;

import com.tulicoreria.licoreria.dto.ClienteRequestDTO;
import com.tulicoreria.licoreria.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", clienteService.listarTodos());
        return "clientes/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", clienteService.buscarPorId(id));
        return "clientes/detalle";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    public String nuevoForm(Model model) {
        model.addAttribute("cliente", new ClienteRequestDTO());
        return "clientes/formulario";
    }

    @PostMapping("/nuevo")
    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    public String crear(@ModelAttribute ClienteRequestDTO dto,
                        RedirectAttributes flash) {
        try {
            clienteService.crear(dto);
            flash.addFlashAttribute("exito", "Cliente registrado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/clientes";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    public String editarForm(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", clienteService.buscarPorId(id));
        return "clientes/formulario";
    }

    @PostMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    public String actualizar(@PathVariable Long id,
                             @ModelAttribute ClienteRequestDTO dto,
                             RedirectAttributes flash) {
        try {
            clienteService.actualizar(id, dto);
            flash.addFlashAttribute("exito", "Cliente actualizado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/clientes";
    }

    // Búsqueda por documento — usado desde el punto de venta (responde JSON)
    @GetMapping("/buscar")
    @ResponseBody
    public Object buscar(@RequestParam String q) {
        if (q.length() < 2) return java.util.List.of();
        try {
            return clienteService.buscarPorDocumento(q);
        } catch (RuntimeException e) {
            return clienteService.buscarPorNombre(q);
        }
    }
}
