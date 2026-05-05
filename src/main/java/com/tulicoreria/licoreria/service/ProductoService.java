package com.tulicoreria.licoreria.service;

import com.tulicoreria.licoreria.dto.ProductoRequestDTO;
import com.tulicoreria.licoreria.dto.ProductoResponseDTO;
import com.tulicoreria.licoreria.model.Producto;

import java.util.List;

public interface ProductoService {

    List<ProductoResponseDTO> listarTodos();
    ProductoResponseDTO buscarPorId(Long id);
    ProductoResponseDTO buscarPorCodigo(String codigo);
    List<ProductoResponseDTO> buscarPorNombre(String nombre);
    List<ProductoResponseDTO> listarConStockBajo();
    List<ProductoResponseDTO> listarSinStock();
    ProductoResponseDTO crear(ProductoRequestDTO dto);
    ProductoResponseDTO actualizar(Long id, ProductoRequestDTO dto);
    void desactivar(Long id);

    // Usado internamente por VentaService y OrdenCompraService
    Producto obtenerEntidad(Long id);
    ProductoResponseDTO toDTO(Producto producto);
}
