package com.remar.EN.gestion.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.remar.EN.gestion.dto.ComprobantesResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ComprobantesService {

    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key}")
    private String apiKey;

    private static final RestClient restClient = RestClient.create();

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite-preview:generateContent?key=";

    public ComprobantesResponseDTO leer(MultipartFile archivo) throws IOException {
        String base64 = Base64.getEncoder().encodeToString(archivo.getBytes());
        String mimeType = resolverMimeType(archivo.getContentType());

        Map<String, Object> schema = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "monto",           Map.of("type", "NUMBER", "description", "Monto de la transferencia como número decimal"),
                        "fecha",           Map.of("type", "STRING", "description", "Fecha en formato YYYY-MM-DD"),
                        "origen",          Map.of("type", "STRING", "description", "Banco o entidad emisora"),
                        "referencia",      Map.of("type", "STRING", "description", "Número de comprobante, operación o CBU"),
                        "clienteSugerido", Map.of("type", "STRING", "description", "Nombre, CUIT o cuenta del ordenante"),
                        "observaciones",   Map.of("type", "STRING", "description", "Concepto u otros datos")
                ),
                "required", List.of("monto", "fecha", "origen", "referencia", "clienteSugerido", "observaciones")
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

        return parsear(rawResponse);
    }

    private ComprobantesResponseDTO parsear(String rawResponse) throws IOException {
        JsonNode root = objectMapper.readTree(rawResponse);
        String text = root.at("/candidates/0/content/parts/0/text").asText();
        text = text.replaceAll("(?s)```json\\s*|```\\s*", "").trim();
        return objectMapper.readValue(text, ComprobantesResponseDTO.class);
    }

    private String resolverMimeType(String contentType) {
        if (contentType == null) return "image/jpeg";
        return switch (contentType.toLowerCase()) {
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
                Reglas:
                - monto: número decimal sin símbolo de moneda
                - fecha: formato YYYY-MM-DD
                - origen: banco o entidad emisora de la transferencia
                - referencia: número de comprobante, operación o CBU de origen
                - clienteSugerido: nombre, Cuit o número de cuenta del ordenante o titular de la cuenta origen
                - observaciones: concepto, tipo de transferencia u otros datos útiles

                Si un campo no está disponible en el comprobante, usá null.
                """;
    }
}
