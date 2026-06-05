package com.tulicoreria.licoreria.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tulicoreria.licoreria.dto.ProductoRequestDTO;
import com.tulicoreria.licoreria.dto.ProductoResponseDTO;
import com.tulicoreria.licoreria.model.Categoria;
import com.tulicoreria.licoreria.model.Producto;
import com.tulicoreria.licoreria.model.Promocion;
import com.tulicoreria.licoreria.model.Proveedor;
import com.tulicoreria.licoreria.repository.CategoriaRepository;
import com.tulicoreria.licoreria.repository.ProductoRepository;
import com.tulicoreria.licoreria.repository.ProveedorRepository;
import com.tulicoreria.licoreria.service.ProductoService;
import com.tulicoreria.licoreria.service.PromocionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProveedorRepository proveedorRepository;
    private final ImagenService imagenService;
    @Lazy private final PromocionService promocionService;

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listarTodos() {
        Map<Long, Promocion> promoDirecta = promocionService.mapPromoDirectaActiva();
        Map<Long, Promocion> promoVolumen = promocionService.mapPromoVolumenActiva();
        return productoRepository.findByActivoTrue().stream()
                .map(p -> toDTOConPromo(p, promoDirecta.get(p.getId()), promoVolumen.get(p.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponseDTO buscarPorId(Long id) {
        return toDTO(productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponseDTO buscarPorCodigo(String codigo) {
        return toDTO(productoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con código: " + codigo)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre)
                .stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listarConStockBajo() {
        return productoRepository.findProductosConStockBajo()
                .stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listarSinStock() {
        return productoRepository.findProductosSinStock()
                .stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional
    public ProductoResponseDTO crear(ProductoRequestDTO dto) {
        if (dto.getCodigo() != null && productoRepository.existsByCodigo(dto.getCodigo())) {
            throw new RuntimeException("Ya existe un producto con el código: " + dto.getCodigo());
        }
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        Proveedor proveedor = proveedorRepository.findById(dto.getProveedorId())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        String imagenRuta = null;
        if (dto.getImagenFile() != null && !dto.getImagenFile().isEmpty()) {
            imagenRuta = imagenService.guardarImagenProducto(categoria.getNombre(), dto.getImagenFile());
        }

        Producto producto = Producto.builder()
                .nombre(dto.getNombre())
                .codigo(dto.getCodigo())
                .descripcion(dto.getDescripcion())
                .marca(dto.getMarca())
                .paisOrigen(dto.getPaisOrigen())
                .cantidadPresentacion(dto.getCantidadPresentacion())
                .unidadPresentacion(dto.getUnidadPresentacion())
                .gradoAlcohol(dto.getGradoAlcohol())
                .precioCompra(dto.getPrecioCompra())
                .precioVenta(dto.getPrecioVenta())
                .stock(dto.getStock() != null ? dto.getStock() : 0)
                .stockMinimo(dto.getStockMinimo() != null ? dto.getStockMinimo() : 0)
                .fechaVencimiento(dto.getFechaVencimiento())
                .imagen(imagenRuta)
                .categoria(categoria)
                .proveedor(proveedor)
                .activo(true)
                .build();

        return toDTO(productoRepository.save(producto));
    }

    @Override
    @Transactional
    public ProductoResponseDTO actualizar(Long id, ProductoRequestDTO dto) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        Proveedor proveedor = proveedorRepository.findById(dto.getProveedorId())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        producto.setNombre(dto.getNombre());
        producto.setCodigo(dto.getCodigo());
        producto.setDescripcion(dto.getDescripcion());
        producto.setMarca(dto.getMarca());
        producto.setPaisOrigen(dto.getPaisOrigen());
        producto.setCantidadPresentacion(dto.getCantidadPresentacion());
        producto.setUnidadPresentacion(dto.getUnidadPresentacion());
        producto.setGradoAlcohol(dto.getGradoAlcohol());
        producto.setPrecioCompra(dto.getPrecioCompra());
        producto.setPrecioVenta(dto.getPrecioVenta());
        producto.setFechaVencimiento(dto.getFechaVencimiento());

        if (dto.getImagenFile() != null && !dto.getImagenFile().isEmpty()) {
            if (producto.getImagen() != null) {
                imagenService.eliminarImagen(producto.getImagen());
            }
            String imagenRuta = imagenService.guardarImagenProducto(categoria.getNombre(), dto.getImagenFile());
            producto.setImagen(imagenRuta);
        }

        if (dto.getStock() != null) {
            producto.setStock(dto.getStock());
        }
        producto.setStockMinimo(dto.getStockMinimo() != null ? dto.getStockMinimo() : 0);
        producto.setCategoria(categoria);
        producto.setProveedor(proveedor);

        return toDTO(productoRepository.save(producto));
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public Producto obtenerEntidad(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponseDTO toDTO(Producto p) {
        Optional<Promocion> directa = promocionService.findPromocionActivaDirecta(p.getId());
        Optional<Promocion> volumen = promocionService.findPromocionActivaVolumen(p.getId());
        return toDTOConPromo(p, directa.orElse(null), volumen.orElse(null));
    }

    /** Construye el DTO con datos de promoción ya resueltos (evita N+1 en listados). */
    private ProductoResponseDTO toDTOConPromo(Producto p, Promocion directa, Promocion volumen) {
        int stockActual = p.getStock() != null ? p.getStock() : 0;
        int stockMin    = p.getStockMinimo() != null ? p.getStockMinimo() : 0;

        // Resolución del precio final y campos de promo
        java.math.BigDecimal precioFinal    = p.getPrecioVenta();
        java.math.BigDecimal precioOriginal = null;
        String  etiquetaPromo  = null;
        boolean tienePromocion = false;
        Long    promocionId    = null;
        String  tipoPromocion  = null;

        if (directa != null) {
            precioOriginal = p.getPrecioVenta();
            precioFinal    = promocionService.calcularPrecioConDescuento(precioOriginal, directa);
            etiquetaPromo  = directa.etiquetaCorta();
            tienePromocion = true;
            promocionId    = directa.getId();
            tipoPromocion  = "DESCUENTO_DIRECTO";
        } else if (volumen != null) {
            etiquetaPromo  = volumen.etiquetaCorta();
            tienePromocion = true;
            promocionId    = volumen.getId();
            tipoPromocion  = "VOLUMEN";
        }

        return ProductoResponseDTO.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .codigo(p.getCodigo())
                .descripcion(p.getDescripcion())
                .marca(p.getMarca())
                .paisOrigen(p.getPaisOrigen())
                .cantidadPresentacion(p.getCantidadPresentacion())
                .unidadPresentacion(p.getUnidadPresentacion())
                .gradoAlcohol(p.getGradoAlcohol())
                .precioCompra(p.getPrecioCompra())
                .precioVenta(precioFinal)
                .precioOriginal(precioOriginal)
                .etiquetaPromo(etiquetaPromo)
                .tienePromocion(tienePromocion)
                .promocionId(promocionId)
                .tipoPromocion(tipoPromocion)
                .stock(stockActual)
                .stockMinimo(stockMin)
                .fechaVencimiento(p.getFechaVencimiento())
                .activo(p.isActivo())
                .categoriaId(p.getCategoria() != null ? p.getCategoria().getId() : null)
                .proveedorId(p.getProveedor() != null ? p.getProveedor().getId() : null)
                .categoriaNombre(p.getCategoria() != null ? p.getCategoria().getNombre() : "Sin Categoría")
                .proveedorRazonSocial(p.getProveedor() != null ? p.getProveedor().getRazonSocial() : "Sin proveedor")
                .urlImagen(imagenService.construirUrl(p.getImagen()))
                .stockBajo(stockActual <= stockMin)
                .sinStock(stockActual == 0)
                .build();
    }

    @Override
    @Transactional
    public void actualizarImagen(Long id, String ruta) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        p.setImagen(ruta);
        productoRepository.save(p);
    }
}
