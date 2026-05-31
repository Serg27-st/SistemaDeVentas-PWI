package com.tulicoreria.licoreria.service.impl;

import com.tulicoreria.licoreria.dto.ReclamacionRequestDTO;
import com.tulicoreria.licoreria.dto.ReclamacionResponseDTO;
import com.tulicoreria.licoreria.model.Reclamacion;
import com.tulicoreria.licoreria.model.Reclamacion.EstadoReclamo;
import com.tulicoreria.licoreria.model.Reclamacion.TipoReclamo;
import com.tulicoreria.licoreria.repository.ReclamacionRepository;
import com.tulicoreria.licoreria.service.NotificacionMailService;
import com.tulicoreria.licoreria.service.ReclamacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReclamacionServiceImpl implements ReclamacionService {

    private final ReclamacionRepository reclamacionRepository;

    // Opcional — solo existe si app.mail.enabled=true
    @Autowired(required = false)
    private NotificacionMailService mailService;

    @Override
    @Transactional
    public ReclamacionResponseDTO registrar(ReclamacionRequestDTO dto) {
        Reclamacion r = Reclamacion.builder()
                .nombre(dto.getNombre().trim())
                .apellido(dto.getApellido().trim())
                .email(dto.getEmail().trim())
                .telefono(dto.getTelefono())
                .tipoReclamo(TipoReclamo.valueOf(dto.getTipoReclamo()))
                .descripcion(dto.getDescripcion().trim())
                .productoAfectado(dto.getProductoAfectado())
                .fechaRegistro(LocalDateTime.now())
                .estado(EstadoReclamo.PENDIENTE)
                .build();

        Reclamacion saved = reclamacionRepository.save(r);

        // Enviar alerta por correo si el servicio está activo
        if (mailService != null) {
            mailService.enviarAlertaReclamacion(saved);
        }

        return toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReclamacionResponseDTO> listarTodas() {
        return reclamacionRepository.findAllByOrderByFechaRegistroDesc()
                .stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional
    public ReclamacionResponseDTO atender(Long id) {
        Reclamacion r = reclamacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reclamación no encontrada: " + id));
        r.setEstado(EstadoReclamo.ATENDIDO);
        return toDTO(reclamacionRepository.save(r));
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPendientes() {
        return reclamacionRepository.countByEstado(EstadoReclamo.PENDIENTE);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────
    private ReclamacionResponseDTO toDTO(Reclamacion r) {
        return ReclamacionResponseDTO.builder()
                .id(r.getId())
                .nombre(r.getNombre())
                .apellido(r.getApellido())
                .email(r.getEmail())
                .telefono(r.getTelefono())
                .tipoReclamo(r.getTipoReclamo().name())
                .descripcion(r.getDescripcion())
                .productoAfectado(r.getProductoAfectado())
                .fechaRegistro(r.getFechaRegistro())
                .estado(r.getEstado().name())
                .build();
    }
}
