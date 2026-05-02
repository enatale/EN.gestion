package com.remar.EN.gestion.repository;

import com.remar.EN.gestion.entity.CuentaCliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CuentaClienteRepository extends JpaRepository<CuentaCliente, Long> {
    Optional<CuentaCliente> findByCuentaOrigen(String cuentaOrigen);
}
