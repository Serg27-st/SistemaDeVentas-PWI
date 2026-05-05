package com.tulicoreria.licoreria.service.impl;

import com.tulicoreria.licoreria.dto.ProveedorRequestDTO;
import com.tulicoreria.licoreria.dto.ProveedorResponseDTO;
import com.tulicoreria.licoreria.model.Proveedor;
import com.tulicoreria.licoreria.repository.ProveedorRepository;
import com.tulicoreria.licoreria.service.ProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository proveedorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProveedorResponseDTO> listarTodos() {
        return proveedorRepository.findAll().stream()
                .map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProveedorResponseDTO> listarActivos() {
        return proveedorRepository.findByActivoTrue().stream()
                .map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProveedorResponseDTO buscarPorId(Long id) {
        return toDTO(proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con id: " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public ProveedorResponseDTO buscarPorRuc(String ruc) {
        return toDTO(proveedorRepository.findByRuc(ruc)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con RUC: " + ruc)));
    }

    @Override
    @Transactional
    public ProveedorResponseDTO crear(ProveedorRequestDTO dto) {
        if (proveedorRepository.existsByRuc(dto.getRuc())) {
            throw new RuntimeException("Ya existe un proveedor con el RUC: " + dto.getRuc());
        }
        Proveedor proveedor = Proveedor.builder()
                .razonSocial(dto.getRazonSocial())
                .ruc(dto.getRuc())
                .direccion(dto.getDireccion())
                .telefono(dto.getTelefono())
                .correo(dto.getCorreo())
                .contactoNombre(dto.getContactoNombre())
                .activo(true)
                .build();
        return toDTO(proveedorRepository.save(proveedor));
    }

    @Override
    @Transactional
    public ProveedorResponseDTO actualizar(Long id, ProveedorRequestDTO dto) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con id: " + id));
        proveedor.setRazonSocial(dto.getRazonSocial());
        proveedor.setRuc(dto.getRuc());
        proveedor.setDireccion(dto.getDireccion());
        proveedor.setTelefono(dto.getTelefono());
        proveedor.setCorreo(dto.getCorreo());
        proveedor.setContactoNombre(dto.getContactoNombre());
        return toDTO(proveedorRepository.save(proveedor));
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con id: " + id));
        proveedor.setActivo(false);
        proveedorRepository.save(proveedor);
    }

    private ProveedorResponseDTO toDTO(Proveedor p) {
        return ProveedorResponseDTO.builder()
                .id(p.getId())
                .razonSocial(p.getRazonSocial())
                .ruc(p.getRuc())
                .direccion(p.getDireccion())
                .telefono(p.getTelefono())
                .correo(p.getCorreo())
                .contactoNombre(p.getContactoNombre())
                .activo(p.isActivo())
                .totalOrdenes(p.getProductos().size())
                .build();
    }
}