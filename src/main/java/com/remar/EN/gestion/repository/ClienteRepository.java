package com.remar.EN.gestion.repository;

import com.remar.EN.gestion.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByCodigo(String codigo);
}
