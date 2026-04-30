package com.remar.EN.gestion.dto;

import com.remar.EN.gestion.entity.ComprobanteLeido;
import com.remar.EN.gestion.entity.EstadoComprobante;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class ComprobanteResponseDTO {

    private final Long id;
    private final BigDecimal monto;
    private final LocalDate fecha;
    private final String origen;
    private final String referencia;
    private final String cuentaOrigen;
    private final String clienteSugeridoTexto;
    private final Long clienteSugeridoId;
    private final String clienteSugeridoNombre;
    private final Long clienteConfirmadoId;
    private final String clienteConfirmadoNombre;
    private final Long transferenciaId;
    private final String observaciones;
    private final String archivoNombre;
    private final EstadoComprobante estado;
    private final LocalDateTime creadoEn;

    public ComprobanteResponseDTO(ComprobanteLeido c, Long clienteSugeridoId, String clienteSugeridoNombre) {
        this.id = c.getId();
        this.monto = c.getMonto();
        this.fecha = c.getFecha();
        this.origen = c.getOrigen();
        this.referencia = c.getReferencia();
        this.cuentaOrigen = c.getCuentaOrigen();
        this.clienteSugeridoTexto = c.getClienteSugeridoTexto();
        this.clienteSugeridoId = clienteSugeridoId;
        this.clienteSugeridoNombre = clienteSugeridoNombre;
        this.clienteConfirmadoId = c.getCliente() != null ? c.getCliente().getId() : null;
        this.clienteConfirmadoNombre = c.getCliente() != null ? c.getCliente().getNombre() : null;
        this.transferenciaId = c.getTransferencia() != null ? c.getTransferencia().getId() : null;
        this.observaciones = c.getObservaciones();
        this.archivoNombre = c.getArchivoNombre();
        this.estado = c.getEstado();
        this.creadoEn = c.getCreadoEn();
    }
}
