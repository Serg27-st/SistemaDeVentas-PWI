package com.tulicoreria.licoreria.service.impl;

import com.tulicoreria.licoreria.dto.ProductoRequestDTO;
import com.tulicoreria.licoreria.dto.ProductoResponseDTO;
import com.tulicoreria.licoreria.model.Categoria;
import com.tulicoreria.licoreria.model.Producto;
import com.tulicoreria.licoreria.model.Proveedor;
import com.tulicoreria.licoreria.repository.CategoriaRepository;
import com.tulicoreria.licoreria.repository.ProductoRepository;
import com.tulicoreria.licoreria.repository.ProveedorRepository;
import com.tulicoreria.licoreria.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProveedorRepository proveedorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listarTodos() {
        return productoRepository.findByActivoTrue().stream()
                .map(this::toDTO).toList();
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

        Producto producto = Producto.builder()
                .nombre(dto.getNombre())
                .codigo(dto.getCodigo())
                .descripcion(dto.getDescripcion())
                .marca(dto.getMarca())
                .paisOrigen(dto.getPaisOrigen())
                .volumenMl(dto.getVolumenMl())
                .gradoAlcohol(dto.getGradoAlcohol())
                .precioCompra(dto.getPrecioCompra())
                .precioVenta(dto.getPrecioVenta())
                // 🔥 Ahora toma dinámicamente el valor del formulario en vez de 0
                .stock(dto.getStock() != null ? dto.getStock() : 0) 
                .stockMinimo(dto.getStockMinimo())
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
        producto.setVolumenMl(dto.getVolumenMl());
        producto.setGradoAlcohol(dto.getGradoAlcohol());
        producto.setPrecioCompra(dto.getPrecioCompra());
        producto.setPrecioVenta(dto.getPrecioVenta());
        // 🔥 Permite actualizar el valor del stock físico desde el formulario de edición
        if (dto.getStock() != null) {
            producto.setStock(dto.getStock());
        }
        producto.setStockMinimo(dto.getStockMinimo());
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
    public Producto obtenerEntidad(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
    }

    @Override
    public ProductoResponseDTO toDTO(Producto p) {
        return ProductoResponseDTO.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .codigo(p.getCodigo())
                .marca(p.getMarca())
                .paisOrigen(p.getPaisOrigen())
                .volumenMl(p.getVolumenMl())
                .gradoAlcohol(p.getGradoAlcohol())
                .precioCompra(p.getPrecioCompra())
                .precioVenta(p.getPrecioVenta())
                .stock(p.getStock())
                .stockMinimo(p.getStockMinimo())
                .activo(p.isActivo())
                .categoriaNombre(p.getCategoria().getNombre())
                .proveedorRazonSocial(p.getProveedor() != null
                        ? p.getProveedor().getRazonSocial() : "Sin proveedor")
                .stockBajo(p.getStock() <= p.getStockMinimo())
                .sinStock(p.getStock() == 0)
                .build();
    }
}