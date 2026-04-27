package com.remar.EN.gestion.dto;

import com.remar.EN.gestion.entity.Cliente;
import lombok.Getter;

@Getter
public class ClienteResponseDTO {

    private final Long id;
    private final String codigo;
    private final String nombre;

    public ClienteResponseDTO(Cliente c) {
        this.id = c.getId();
        this.codigo = c.getCodigo();
        this.nombre = c.getNombre();
    }
}
