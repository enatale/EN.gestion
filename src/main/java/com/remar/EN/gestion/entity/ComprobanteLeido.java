package com.remar.EN.gestion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "comprobantes_leidos")
@Getter
@Setter
public class ComprobanteLeido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 15, scale = 2)
    private BigDecimal monto;

    private LocalDate fecha;

    private String origen;

    private String referencia;

    @Column(name = "cuenta_origen")
    private String cuentaOrigen;

    @Column(name = "cliente_sugerido_texto")
    private String clienteSugeridoTexto;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "archivo_nombre")
    private String archivoNombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoComprobante estado = EstadoComprobante.PENDIENTE;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @OneToOne
    @JoinColumn(name = "transferencia_id")
    private Transferencia transferencia;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    private void prePersist() {
        creadoEn = LocalDateTime.now();
    }
}
