package com.remar.EN.gestion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "cuentas_clientes")
@Getter
@Setter
public class CuentaCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cuenta_origen", unique = true, nullable = false)
    private String cuentaOrigen;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    @PrePersist
    @PreUpdate
    private void preUpdate() {
        actualizadoEn = LocalDateTime.now();
    }
}
