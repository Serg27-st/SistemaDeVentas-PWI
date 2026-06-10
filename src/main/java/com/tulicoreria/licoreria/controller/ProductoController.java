package com.tulicoreria.licoreria.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tulicoreria.licoreria.dto.ProductoRequestDTO;
import com.tulicoreria.licoreria.dto.ProductoResponseDTO;
import com.tulicoreria.licoreria.service.CategoriaService;
import com.tulicoreria.licoreria.service.ProductoService;
import com.tulicoreria.licoreria.service.ProveedorService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    private final ProveedorService proveedorService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("productos", productoService.listarTodos());
        model.addAttribute("stockBajo", productoService.listarConStockBajo());
        model.addAttribute("categorias", categoriaService.listarActivas());
        return "productos/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("producto", productoService.buscarPorId(id));
        return "productos/detalle";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyRole('ADMIN','ALMACEN')")
    public String nuevoForm(Model model) {
        model.addAttribute("producto", new ProductoRequestDTO());
        model.addAttribute("categorias", categoriaService.listarActivas());
        model.addAttribute("proveedores", proveedorService.listarActivos());
        return "productos/formulario";
    }

    @PostMapping("/nuevo")
    @PreAuthorize("hasAnyRole('ADMIN','ALMACEN')")
    public String crear(@ModelAttribute ProductoRequestDTO dto, RedirectAttributes flash) {
        try {
            productoService.crear(dto);
            flash.addFlashAttribute("exito", "Producto registrado correctamente");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/productos";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ALMACEN')")
    public String editarForm(@PathVariable Long id, Model model) {
        ProductoResponseDTO response = productoService.buscarPorId(id);
        ProductoRequestDTO requestDto = mapearARequestDTO(response);

        model.addAttribute("producto", requestDto);
        model.addAttribute("categorias", categoriaService.listarActivas());
        model.addAttribute("proveedores", proveedorService.listarActivos());
        return "productos/formulario";
    }

    @PostMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ALMACEN')")
    public String actualizar(@PathVariable Long id,
            @ModelAttribute ProductoRequestDTO dto,
            RedirectAttributes flash) {
        try {
            productoService.actualizar(id, dto);
            flash.addFlashAttribute("exito", "Producto actualizado correctamente");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/productos";
    }

    @PostMapping("/desactivar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String desactivar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            productoService.desactivar(id);
            flash.addFlashAttribute("exito", "Producto desactivado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/productos";
    }

    @GetMapping("/buscar")
    @ResponseBody
    public Object buscar(@RequestParam String q) {
        if (q.length() < 2) {
            return java.util.List.of();
        }
        try {
            return java.util.List.of(productoService.buscarPorCodigo(q));
        } catch (RuntimeException e) {
            return productoService.buscarPorNombre(q);
        }
    }

    // Mapeo utilitario interno para mantener limpio el endpoint de edición
    private ProductoRequestDTO mapearARequestDTO(ProductoResponseDTO response) {
        return ProductoRequestDTO.builder()
                .id(response.getId())
                .nombre(response.getNombre())
                .codigo(response.getCodigo())
                .descripcion(response.getDescripcion())
                .marca(response.getMarca())
                .paisOrigen(response.getPaisOrigen())
                .cantidad(response.getCantidad())
                .unidadMedida(response.getUnidadMedida())
                .gradoAlcohol(response.getGradoAlcohol())
                .precioCompra(response.getPrecioCompra())
                .precioVenta(response.getPrecioVenta())
                .stock(response.getStock())
                .stockMinimo(response.getStockMinimo())
                .fechaVencimiento(response.getFechaVencimiento())
                .categoriaId(response.getCategoriaId())
                .proveedorId(response.getProveedorId())
                .imagen(extraerRutaRelativaImagen(response.getUrlImagen()))
                .build();
    }

    private String extraerRutaRelativaImagen(String urlImagen) {
        if (urlImagen == null || urlImagen.isBlank()) {
            return null;
        }
        return urlImagen.startsWith("/images/") ? urlImagen.substring("/images/".length()) : urlImagen;
    }
}
