package com.tulicoreria.licoreria.repository;

import com.tulicoreria.licoreria.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Optional<Pedido> findByNumeroPedido(String numeroPedido);

    List<Pedido> findByClienteWebIdOrderByCreadoEnDesc(Long clienteWebId);

    List<Pedido> findByEmailClienteOrderByCreadoEnDesc(String email);

    /** Pedidos expirados aún en PENDIENTE_PAGO (para liberar stock). */
    @Query("SELECT p FROM Pedido p WHERE p.estado = 'PENDIENTE_PAGO' AND p.expiraEn < :ahora")
    List<Pedido> findExpirados(LocalDateTime ahora);

    /** Último número de pedido para generar el siguiente correlativo. */
    @Query("SELECT MAX(p.numeroPedido) FROM Pedido p WHERE p.numeroPedido LIKE 'PED-%'")
    String findUltimoNumeroPedido();
}
