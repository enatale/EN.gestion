package com.remar.EN.gestion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class TransferenciaRequestDTO {

    @NotNull
    @Positive
    private BigDecimal monto;

    @NotNull
    private LocalDate fecha;

    @NotBlank
    private String origen;

    private String referencia;

    @NotBlank
    private String cliente;

    private String observaciones;
}
