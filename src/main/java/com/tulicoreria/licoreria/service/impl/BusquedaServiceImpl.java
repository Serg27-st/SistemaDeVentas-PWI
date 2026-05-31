package com.tulicoreria.licoreria.service.impl;

import com.tulicoreria.licoreria.dto.BusquedaResultDTO;
import com.tulicoreria.licoreria.dto.ProductoResponseDTO;
import com.tulicoreria.licoreria.model.BusquedaLog;
import com.tulicoreria.licoreria.repository.BusquedaLogRepository;
import com.tulicoreria.licoreria.service.BusquedaService;
import com.tulicoreria.licoreria.service.ProductoService;
import com.tulicoreria.licoreria.util.FuzzySearchUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusquedaServiceImpl implements BusquedaService {

    private final ProductoService productoService;
    private final BusquedaLogRepository logRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BusquedaResultDTO> buscar(String query, int limit) {
        String q = (query == null) ? "" : query.trim();
        if (q.length() < 2) return List.of();

        // Calcular relevancia para cada producto activo
        record ScoredProduct(ProductoResponseDTO p, double score) {}

        return productoService.listarTodos().stream()
                .filter(ProductoResponseDTO::isActivo)
                .map(p -> new ScoredProduct(p, FuzzySearchUtil.scoreProducto(p, q)))
                .filter(sp -> sp.score() >= FuzzySearchUtil.THRESHOLD)
                .sorted(Comparator.comparingDouble(ScoredProduct::score).reversed())
                .limit(limit)
                .map(sp -> toDTO(sp.p()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> tendencias(int limit) {
        return logRepository.findTop6ByOrderByConteoDesc().stream()
                .map(BusquedaLog::getTermino)
                .limit(limit)
                .toList();
    }

    /**
     * Registra o incrementa el contador de un término de búsqueda.
     * Se ejecuta de forma asíncrona para no bloquear la respuesta al usuario.
     */
    @Override
    @Async
    @Transactional
    public void registrarBusqueda(String termino) {
        if (termino == null || termino.isBlank() || termino.length() < 2) return;
        String t = termino.trim().toLowerCase(Locale.ROOT);
        logRepository.findByTermino(t).ifPresentOrElse(
                log -> {
                    log.setConteo(log.getConteo() + 1);
                    log.setUltimaBusqueda(LocalDateTime.now());
                    logRepository.save(log);
                },
                () -> logRepository.save(BusquedaLog.builder()
                        .termino(t)
                        .conteo(1L)
                        .ultimaBusqueda(LocalDateTime.now())
                        .build())
        );
    }

    // ── Mapper ─────────────────────────────────────────────────────────────────
    private BusquedaResultDTO toDTO(ProductoResponseDTO p) {
        return BusquedaResultDTO.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .marca(p.getMarca())
                .categoriaNombre(p.getCategoriaNombre())
                .volumenMl(p.getVolumenMl())
                .precioVenta(p.getPrecioVenta())
                .urlImagen(p.getUrlImagen())
                .sinStock(p.isSinStock())
                .build();
    }
}
