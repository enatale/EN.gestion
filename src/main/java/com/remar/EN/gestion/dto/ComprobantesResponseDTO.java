package com.remar.EN.gestion.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComprobantesResponseDTO {

    private BigDecimal monto;
    private String fecha;           // YYYY-MM-DD
    private String origen;          // banco o entidad emisora
    private String referencia;      // número de comprobante/operación
    private String clienteSugerido; // nombre del ordenante
    private String observaciones;
}