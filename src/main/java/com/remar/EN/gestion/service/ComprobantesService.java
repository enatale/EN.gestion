package com.remar.EN.gestion.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.remar.EN.gestion.dto.ComprobanteResponseDTO;
import com.remar.EN.gestion.dto.ComprobantesResponseDTO;
import com.remar.EN.gestion.dto.ConfirmarComprobanteRequestDTO;
import com.remar.EN.gestion.entity.*;
import com.remar.EN.gestion.exception.CuentaClienteConflictException;
import com.remar.EN.gestion.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComprobantesService {

    private final ObjectMapper objectMapper;
    private final ComprobanteLeidoRepository comprobanteLeidoRepository;
    private final CuentaClienteRepository cuentaClienteRepository;
    private final ClienteRepository clienteRepository;
    private final TransferenciaRepository transferenciaRepository;

    @Value("${gemini.api-key}")
    private String apiKey;

    private static final RestClient restClient = RestClient.create();

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite-preview:generateContent?key=";

    @Transactional
    public ComprobanteResponseDTO leer(MultipartFile archivo) {
        byte[] bytes;
        try {
            bytes = archivo.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo leer el archivo subido", e);
        }
        return leerBytes(bytes, resolverMimeType(archivo.getContentType()), archivo.getOriginalFilename());
    }

    @Transactional
    public ComprobanteResponseDTO leerBytes(byte[] bytes, String mimeType, String archivoNombre) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY no configurada en el servidor");
        }

        String base64 = Base64.getEncoder().encodeToString(bytes);

        Map<String, Object> schema = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "monto",          Map.of("type", "NUMBER",  "description", "Monto de la transferencia como número decimal"),
                        "fecha",          Map.of("type", "STRING",  "description", "Fecha en formato YYYY-MM-DD"),
                        "origen",         Map.of("type", "STRING",  "description", "Banco o entidad emisora"),
                        "referencia",     Map.of("type", "STRING",  "description", "Número de comprobante u operación"),
                        "cuentaOrigen",   Map.of("type", "STRING",  "description", "CBU, CVU o número de cuenta del ordenante (sin nombre)"),
                        "clienteSugerido",Map.of("type", "STRING",  "description", "Nombre, CUIT o razón social del ordenante"),
                        "observaciones",  Map.of("type", "STRING",  "description", "Concepto u otros datos")
                )
        );

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(
                                Map.of("text", prompt()),
                                Map.of("inline_data", Map.of("mime_type", mimeType, "data", base64))
                        )
                )),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json",
                        "responseSchema", schema
                )
        );

        String rawResponse = restClient.post()
                .uri(GEMINI_URL + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        ComprobantesResponseDTO gemini;
        try {
            gemini = parsear(rawResponse);
        } catch (JsonProcessingException e) {
            log.error("No se pudo parsear la respuesta de Gemini: {}", rawResponse, e);
            throw new IllegalStateException("Respuesta de Gemini no válida: " + e.getMessage(), e);
        }

        ComprobanteLeido comprobante = new ComprobanteLeido();
        comprobante.setMonto(gemini.getMonto());
        comprobante.setFecha(parseFecha(gemini.getFecha()));
        comprobante.setOrigen(gemini.getOrigen());
        comprobante.setReferencia(gemini.getReferencia());
        comprobante.setCuentaOrigen(gemini.getCuentaOrigen());
        comprobante.setClienteSugeridoTexto(gemini.getClienteSugerido());
        comprobante.setObservaciones(gemini.getObservaciones());
        comprobante.setArchivoNombre(archivoNombre);

        ComprobanteLeido saved = comprobanteLeidoRepository.save(comprobante);
        return toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ComprobanteResponseDTO> listar(EstadoComprobante estado) {
        List<ComprobanteLeido> comprobantes = estado != null
                ? comprobanteLeidoRepository.findByEstado(estado)
                : comprobanteLeidoRepository.findAll();

        return comprobantes.stream().map(this::toResponseDTO).toList();
    }

    @Transactional
    public ComprobanteResponseDTO confirmar(Long id, ConfirmarComprobanteRequestDTO dto) {
        ComprobanteLeido comprobante = comprobanteLeidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comprobante no encontrado: " + id));

        if (comprobante.getEstado() != EstadoComprobante.PENDIENTE) {
            throw new IllegalStateException("El comprobante ya fue " + comprobante.getEstado().name().toLowerCase());
        }

        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + dto.getClienteId()));

        BigDecimal monto = dto.getMonto() != null ? dto.getMonto() : comprobante.getMonto();
        LocalDate fecha = dto.getFecha() != null ? dto.getFecha() : comprobante.getFecha();
        String origen = dto.getOrigen() != null ? dto.getOrigen() : comprobante.getOrigen();
        String cuentaOrigen = dto.getCuentaOrigen() != null ? dto.getCuentaOrigen() : comprobante.getCuentaOrigen();
        String observaciones = dto.getObservaciones() != null ? dto.getObservaciones() : comprobante.getObservaciones();

        if (monto == null) throw new IllegalArgumentException("El monto es requerido para confirmar");
        if (fecha == null) throw new IllegalArgumentException("La fecha es requerida para confirmar");

        manejarCuentaCliente(cuentaOrigen, cliente, dto.getActualizarCuenta());

        String referencia = dto.getReferencia() != null ? dto.getReferencia() : comprobante.getReferencia();
        if (referencia != null && transferenciaRepository.existsByReferencia(referencia)) {
            throw new IllegalArgumentException("Ya existe una transferencia con esa referencia: " + referencia);
        }

        Transferencia t = new Transferencia();
        t.setMonto(monto);
        t.setFecha(fecha);
        t.setOrigen(origen);
        t.setReferencia(referencia);
        t.setCliente(cliente);
        t.setObservaciones(observaciones);
        Transferencia transferencia = transferenciaRepository.save(t);

        comprobante.setEstado(EstadoComprobante.CONFIRMADO);
        comprobante.setCliente(cliente);
        comprobante.setTransferencia(transferencia);
        return toResponseDTO(comprobanteLeidoRepository.save(comprobante));
    }

    @Transactional
    public ComprobanteResponseDTO descartar(Long id) {
        ComprobanteLeido comprobante = comprobanteLeidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comprobante no encontrado: " + id));

        if (comprobante.getEstado() != EstadoComprobante.PENDIENTE) {
            throw new IllegalStateException("El comprobante ya fue " + comprobante.getEstado().name().toLowerCase());
        }

        comprobante.setEstado(EstadoComprobante.DESCARTADO);
        return toResponseDTO(comprobanteLeidoRepository.save(comprobante));
    }

    private void manejarCuentaCliente(String cuentaOrigen, Cliente cliente, Boolean actualizarCuenta) {
        if (cuentaOrigen == null) return;

        Optional<CuentaCliente> existente = cuentaClienteRepository.findByCuentaOrigen(cuentaOrigen);

        if (existente.isEmpty()) {
            CuentaCliente nueva = new CuentaCliente();
            nueva.setCuentaOrigen(cuentaOrigen);
            nueva.setCliente(cliente);
            cuentaClienteRepository.save(nueva);
            return;
        }

        CuentaCliente cc = existente.get();
        if (cc.getCliente().getId().equals(cliente.getId())) return;

        if (actualizarCuenta == null) {
            throw new CuentaClienteConflictException(cuentaOrigen, cc.getCliente());
        }

        if (Boolean.TRUE.equals(actualizarCuenta)) {
            cc.setCliente(cliente);
            cuentaClienteRepository.save(cc);
        }
    }

    private ComprobanteResponseDTO toResponseDTO(ComprobanteLeido c) {
        Long sugeridoId = null;
        String sugeridoNombre = null;
        if (c.getEstado() == EstadoComprobante.PENDIENTE && c.getCuentaOrigen() != null) {
            Optional<CuentaCliente> cc = cuentaClienteRepository.findByCuentaOrigen(c.getCuentaOrigen());
            if (cc.isPresent()) {
                sugeridoId = cc.get().getCliente().getId();
                sugeridoNombre = cc.get().getCliente().getNombre();
            }
        }
        return new ComprobanteResponseDTO(c, sugeridoId, sugeridoNombre);
    }

    private ComprobantesResponseDTO parsear(String rawResponse) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(rawResponse);
        String text = root.at("/candidates/0/content/parts/0/text").asText();
        text = text.replaceAll("(?s)```json\\s*|```\\s*", "").trim();
        return objectMapper.readValue(text, ComprobantesResponseDTO.class);
    }

    private LocalDate parseFecha(String fecha) {
        if (fecha == null) return null;
        try {
            return LocalDate.parse(fecha);
        } catch (DateTimeParseException e) {
            log.warn("No se pudo parsear la fecha '{}' extraída por Gemini", fecha);
            return null;
        }
    }

    private String resolverMimeType(String contentType) {
        if (contentType == null) return "image/jpeg";
        String base = contentType.split(";")[0].trim().toLowerCase();
        return switch (base) {
            case "application/pdf" -> "application/pdf";
            case "image/png"       -> "image/png";
            case "image/gif"       -> "image/gif";
            case "image/webp"      -> "image/webp";
            default                -> "image/jpeg";
        };
    }

    private String prompt() {
        return """
                Extrae los datos de este comprobante de transferencia.
                Si el comprobante contiene "BNA+", el origen es Banco Nación
                
                Reglas:
                - monto: número decimal sin símbolo de moneda
                - fecha: formato YYYY-MM-DD
                - origen: banco o entidad emisora de la transferencia
                - referencia: número de comprobante u operación
                - cuentaOrigen: CBU, CVU o número de cuenta del ordenante (solo el número, sin nombre)
                - clienteSugerido: nombre, CUIT o razón social del ordenante o titular de la cuenta origen
                - observaciones: concepto, tipo de transferencia u otros datos útiles

                Si un campo no está disponible en el comprobante, usá null.
                """;
    }
}
