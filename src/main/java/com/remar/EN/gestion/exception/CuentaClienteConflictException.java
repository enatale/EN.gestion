package com.remar.EN.gestion.exception;

import com.remar.EN.gestion.entity.Cliente;
import lombok.Getter;

@Getter
public class CuentaClienteConflictException extends RuntimeException {

    private final String cuentaOrigen;
    private final Long clienteActualId;
    private final String clienteActualNombre;

    public CuentaClienteConflictException(String cuentaOrigen, Cliente clienteActual) {
        super("Conflicto de cuenta");
        this.cuentaOrigen = cuentaOrigen;
        this.clienteActualId = clienteActual.getId();
        this.clienteActualNombre = clienteActual.getNombre();
    }
}
