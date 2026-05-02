package com.remar.EN.gestion.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ConfirmarComprobanteRequestDTO {

    @NotNull
    private Long clienteId;

    @Positive
    private BigDecimal monto;

    private LocalDate fecha;

    private String origen;

    private String referencia;

    private String cuentaOrigen;

    private String observaciones;

    private Boolean actualizarCuenta;
}
