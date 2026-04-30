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
    private String origen;
    private String referencia;
    private String cuentaOrigen;    // CBU, CVU o número de cuenta del ordenante
    private String clienteSugerido; // nombre del ordenante
    private String observaciones;
}