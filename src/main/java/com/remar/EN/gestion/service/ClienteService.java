package com.remar.EN.gestion.service;

import com.remar.EN.gestion.dto.ClienteRequestDTO;
import com.remar.EN.gestion.dto.ClienteResponseDTO;
import com.remar.EN.gestion.entity.Cliente;
import com.remar.EN.gestion.repository.ClienteRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository repository;

    @Transactional
    public ClienteResponseDTO crear(ClienteRequestDTO dto) {
        if (repository.existsByCodigo(dto.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un cliente con el código: " + dto.getCodigo());
        }
        Cliente c = new Cliente();
        c.setCodigo(dto.getCodigo());
        c.setNombre(dto.getNombre());
        return new ClienteResponseDTO(repository.save(c));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listar() {
        return repository.findAll().stream()
                .map(ClienteResponseDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO obtener(Long id) {
        return repository.findById(id)
                .map(ClienteResponseDTO::new)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + id));
    }
}
