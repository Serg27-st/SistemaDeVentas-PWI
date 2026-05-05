package com.tulicoreria.licoreria.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tulicoreria.licoreria.model.OrdenCompra;
import com.tulicoreria.licoreria.model.OrdenCompra.EstadoOrden;
import com.tulicoreria.licoreria.model.Proveedor;

@Repository
public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {

    Optional<OrdenCompra> findByNumeroOrden(String numeroOrden);

    // Órdenes pendientes de recibir (módulo de almacén)
    List<OrdenCompra> findByEstado(EstadoOrden estado);

    // Historial de compras por proveedor
    List<OrdenCompra> findByProveedorOrderByFechaEmisionDesc(Proveedor proveedor);

    // Último número de orden para generar el siguiente (OC-000002)
    @Query("SELECT MAX(o.numeroOrden) FROM OrdenCompra o")
    String findUltimoNumeroOrden();

    // Cantidad de órdenes pendientes (para dashboard y alertas)
    long countByEstado(EstadoOrden estado);
}
