package com.remar.EN.gestion.repository;

import com.remar.EN.gestion.entity.EstadoTransferencia;
import com.remar.EN.gestion.entity.Transferencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferenciaRepository extends JpaRepository<Transferencia, Long> {
    List<Transferencia> findByEstado(EstadoTransferencia estado);
    List<Transferencia> findByClienteContainingIgnoreCase(String cliente);
    boolean existsByReferencia(String referencia);
}
