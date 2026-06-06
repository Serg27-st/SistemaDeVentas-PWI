package com.tulicoreria.licoreria.controller;

import com.tulicoreria.licoreria.dto.RegistroClienteWebDTO;
import com.tulicoreria.licoreria.dto.VentaResponseDTO;
import com.tulicoreria.licoreria.model.ClienteWeb;
import com.tulicoreria.licoreria.model.DireccionEnvio;
import com.tulicoreria.licoreria.repository.VentaRepository;
import com.tulicoreria.licoreria.service.ClienteWebService;
import com.tulicoreria.licoreria.service.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ClienteWebController {

    private final ClienteWebService clienteWebService;
    private final VentaRepository ventaRepository;
    private final VentaService ventaService;

    // ── Registro ─────────────────────────────────────────────────────────────

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("dto", new RegistroClienteWebDTO());
        return "publica/registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@ModelAttribute RegistroClienteWebDTO dto,
                                   RedirectAttributes flash) {
        try {
            clienteWebService.registrar(dto);
            flash.addFlashAttribute("registroExito", true);
            return "redirect:/login?registrado=true";
        } catch (RuntimeException e) {
            flash.addFlashAttribute("errorRegistro", e.getMessage());
            flash.addFlashAttribute("dto", dto);
            return "redirect:/registro";
        }
    }

    // ── Mi Cuenta — Panel ────────────────────────────────────────────────────

    @GetMapping("/mi-cuenta")
    public String miCuenta(Authentication auth, Model model) {
        ClienteWeb cw = clienteWebService.findByEmail(auth.getName());
        model.addAttribute("cliente", cw);

        long totalPedidos = 0;
        if (cw.getCliente() != null) {
            totalPedidos = ventaRepository.findByClienteIdOrderByFechaHoraDesc(
                    cw.getCliente().getId()).size();
        }
        model.addAttribute("totalPedidos", totalPedidos);
        model.addAttribute("totalDirecciones", cw.getDirecciones().size());
        return "publica/mi-cuenta/index";
    }

    // ── Historial de pedidos ──────────────────────────────────────────────────

    @GetMapping("/mi-cuenta/pedidos")
    public String misPedidos(Authentication auth, Model model) {
        ClienteWeb cw = clienteWebService.findByEmail(auth.getName());
        model.addAttribute("cliente", cw);

        List<VentaResponseDTO> pedidos = List.of();
        if (cw.getCliente() != null) {
            pedidos = ventaRepository
                    .findByClienteIdOrderByFechaHoraDesc(cw.getCliente().getId())
                    .stream()
                    .map(ventaService::toDTO)
                    .toList();
        }
        model.addAttribute("pedidos", pedidos);
        return "publica/mi-cuenta/pedidos";
    }

    // ── Direcciones de envío ──────────────────────────────────────────────────

    @GetMapping("/mi-cuenta/direcciones")
    public String misDirecciones(Authentication auth, Model model) {
        ClienteWeb cw = clienteWebService.findByEmail(auth.getName());
        model.addAttribute("cliente", cw);
        model.addAttribute("direcciones", cw.getDirecciones());
        model.addAttribute("nuevaDireccion", new DireccionEnvio());
        return "publica/mi-cuenta/direcciones";
    }

    @PostMapping("/mi-cuenta/direcciones/nueva")
    public String agregarDireccion(Authentication auth,
                                   @RequestParam String alias,
                                   @RequestParam String direccionCompleta,
                                   @RequestParam(required = false) String referencia,
                                   @RequestParam(required = false) String distrito,
                                   RedirectAttributes flash) {
        try {
            DireccionEnvio dir = DireccionEnvio.builder()
                    .alias(alias)
                    .direccionCompleta(direccionCompleta)
                    .referencia(referencia)
                    .distrito(distrito)
                    .build();
            clienteWebService.agregarDireccion(auth.getName(), dir);
            flash.addFlashAttribute("exito", "Dirección guardada correctamente.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mi-cuenta/direcciones";
    }

    @PostMapping("/mi-cuenta/direcciones/eliminar/{id}")
    public String eliminarDireccion(@PathVariable Long id, Authentication auth,
                                    RedirectAttributes flash) {
        try {
            clienteWebService.eliminarDireccion(auth.getName(), id);
            flash.addFlashAttribute("exito", "Dirección eliminada.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mi-cuenta/direcciones";
    }

    @PostMapping("/mi-cuenta/direcciones/default/{id}")
    public String marcarDefault(@PathVariable Long id, Authentication auth,
                                RedirectAttributes flash) {
        clienteWebService.marcarDefaultDireccion(auth.getName(), id);
        flash.addFlashAttribute("exito", "Dirección predeterminada actualizada.");
        return "redirect:/mi-cuenta/direcciones";
    }

    // ── Perfil ────────────────────────────────────────────────────────────────

    @GetMapping("/mi-cuenta/perfil")
    public String perfil(Authentication auth, Model model) {
        ClienteWeb cw = clienteWebService.findByEmail(auth.getName());
        model.addAttribute("cliente", cw);
        return "publica/mi-cuenta/perfil";
    }

    @PostMapping("/mi-cuenta/perfil/actualizar")
    public String actualizarPerfil(Authentication auth,
                                   @RequestParam String nombre,
                                   @RequestParam String apellido,
                                   @RequestParam(required = false) String telefono,
                                   @RequestParam(required = false) String dni,
                                   RedirectAttributes flash) {
        try {
            clienteWebService.actualizarPerfil(auth.getName(), nombre, apellido, telefono, dni);
            flash.addFlashAttribute("exito", "Perfil actualizado correctamente.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mi-cuenta/perfil";
    }
}
