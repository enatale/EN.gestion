package com.remar.EN.gestion.service;

import com.remar.EN.gestion.dto.TransferenciaRequestDTO;
import com.remar.EN.gestion.dto.TransferenciaResponseDTO;
import com.remar.EN.gestion.entity.Transferencia;
import com.remar.EN.gestion.repository.TransferenciaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferenciaService {

    private final TransferenciaRepository repository;

    @Transactional
    public TransferenciaResponseDTO registrar(TransferenciaRequestDTO dto) {
        if (dto.getReferencia() != null && repository.existsByReferencia(dto.getReferencia())) {
            throw new IllegalArgumentException("Ya existe una transferencia con esa referencia: " + dto.getReferencia());
        }

        Transferencia t = new Transferencia();
        t.setMonto(dto.getMonto());
        t.setFecha(dto.getFecha());
        t.setOrigen(dto.getOrigen());
        t.setReferencia(dto.getReferencia());
        t.setCliente(dto.getCliente());
        t.setObservaciones(dto.getObservaciones());

        return new TransferenciaResponseDTO(repository.save(t));
    }

    @Transactional(readOnly = true)
    public List<TransferenciaResponseDTO> listar() {
        return repository.findAll().stream()
                .map(TransferenciaResponseDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransferenciaResponseDTO obtener(Long id) {
        return repository.findById(id)
                .map(TransferenciaResponseDTO::new)
                .orElseThrow(() -> new EntityNotFoundException("Transferencia no encontrada: " + id));
    }
}
