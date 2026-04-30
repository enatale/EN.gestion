package com.remar.EN.gestion.controller;

import com.remar.EN.gestion.dto.ComprobanteResponseDTO;
import com.remar.EN.gestion.dto.ConfirmarComprobanteRequestDTO;
import com.remar.EN.gestion.entity.EstadoComprobante;
import com.remar.EN.gestion.exception.CuentaClienteConflictException;
import com.remar.EN.gestion.service.ComprobantesService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/comprobantes")
@RequiredArgsConstructor
public class ComprobantesController {

    private final ComprobantesService service;

    @PostMapping("/leer")
    public ResponseEntity<ComprobanteResponseDTO> leer(
            @RequestParam("archivo") MultipartFile archivo) {
        if (archivo.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.leer(archivo));
    }

    @GetMapping
    public ResponseEntity<List<ComprobanteResponseDTO>> listar(
            @RequestParam(required = false) EstadoComprobante estado) {
        return ResponseEntity.ok(service.listar(estado));
    }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<ComprobanteResponseDTO> confirmar(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmarComprobanteRequestDTO dto) {
        return ResponseEntity.ok(service.confirmar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ComprobanteResponseDTO> descartar(@PathVariable Long id) {
        return ResponseEntity.ok(service.descartar(id));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleServerError(IllegalStateException ex) {
        return ResponseEntity.internalServerError().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(CuentaClienteConflictException.class)
    public ResponseEntity<Map<String, Object>> handleCuentaConflict(CuentaClienteConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "La cuenta '" + ex.getCuentaOrigen() + "' ya está asignada al cliente '"
                        + ex.getClienteActualNombre() + "'. Reenvíe con actualizarCuenta: true para actualizarla, o false para mantenerla.",
                "clienteActualId", ex.getClienteActualId(),
                "clienteActualNombre", ex.getClienteActualNombre(),
                "cuentaOrigen", ex.getCuentaOrigen()
        ));
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<Map<String, String>> handleGeminiError(RestClientResponseException ex) {
        log.error("Gemini API error {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", "Error al consultar Gemini (" + ex.getStatusCode() + ")"));
    }
}
