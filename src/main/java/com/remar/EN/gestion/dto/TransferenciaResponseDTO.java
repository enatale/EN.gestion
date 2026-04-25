package com.remar.EN.gestion.dto;

import com.remar.EN.gestion.entity.EstadoTransferencia;
import com.remar.EN.gestion.entity.Transferencia;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class TransferenciaResponseDTO {

    private final Long id;
    private final BigDecimal monto;
    private final LocalDate fecha;
    private final String origen;
    private final String referencia;
    private final String cliente;
    private final String observaciones;
    private final EstadoTransferencia estado;
    private final LocalDateTime creadoEn;

    public TransferenciaResponseDTO(Transferencia t) {
        this.id = t.getId();
        this.monto = t.getMonto();
        this.fecha = t.getFecha();
        this.origen = t.getOrigen();
        this.referencia = t.getReferencia();
        this.cliente = t.getCliente();
        this.observaciones = t.getObservaciones();
        this.estado = t.getEstado();
        this.creadoEn = t.getCreadoEn();
    }
}
