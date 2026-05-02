package com.remar.EN.gestion.repository;

import com.remar.EN.gestion.entity.ComprobanteLeido;
import com.remar.EN.gestion.entity.EstadoComprobante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComprobanteLeidoRepository extends JpaRepository<ComprobanteLeido, Long> {
    List<ComprobanteLeido> findByEstado(EstadoComprobante estado);
}
