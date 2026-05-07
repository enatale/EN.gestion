package com.remar.EN.gestion.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.remar.EN.gestion.dto.WhatsAppWebhookPayload;
import com.remar.EN.gestion.dto.WhatsAppWebhookPayload.Message;
import com.remar.EN.gestion.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppWebhookService {

    private final UsuarioRepository usuarioRepository;
    private final ComprobantesService comprobantesService;
    private final ObjectMapper objectMapper;

    @Value("${whatsapp.access-token}")
    private String accessToken;

    private static final RestClient restClient = RestClient.create();
    private static final String GRAPH_API = "https://graph.facebook.com/v18.0/";

    public void procesarMensaje(WhatsAppWebhookPayload payload) {
        if (!"whatsapp_business_account".equals(payload.getObject())) return;
        if (payload.getEntry() == null) return;

        payload.getEntry().stream()
                .filter(e -> e.getChanges() != null)
                .flatMap(e -> e.getChanges().stream())
                .filter(c -> c.getValue() != null && c.getValue().getMessages() != null)
                .flatMap(c -> c.getValue().getMessages().stream())
                .forEach(this::procesarMensajeIndividual);
    }

    private void procesarMensajeIndividual(Message mensaje) {
        String from = mensaje.getFrom();
        if (from == null) return;

        if (usuarioRepository.findByWhatsappNumero(from).isEmpty()) {
            log.info("Mensaje de WhatsApp ignorado: número {} no registrado en el sistema", from);
            return;
        }

        String mediaId;
        String mimeType;
        String filename;

        if ("image".equals(mensaje.getType()) && mensaje.getImage() != null) {
            mediaId = mensaje.getImage().getId();
            mimeType = mensaje.getImage().getMimeType();
            filename = "whatsapp_" + mediaId + "." + extensionDeMimeType(mimeType);
        } else if ("document".equals(mensaje.getType()) && mensaje.getDocument() != null) {
            mediaId = mensaje.getDocument().getId();
            mimeType = mensaje.getDocument().getMimeType();
            filename = mensaje.getDocument().getFilename() != null
                    ? mensaje.getDocument().getFilename()
                    : "whatsapp_" + mediaId + "." + extensionDeMimeType(mimeType);
        } else {
            log.info("Mensaje de WhatsApp ignorado: tipo '{}' no soportado (número: {})", mensaje.getType(), from);
            return;
        }

        try {
            byte[] bytes = descargarMedia(mediaId);
            comprobantesService.leerBytes(bytes, mimeType, filename);
            log.info("Comprobante procesado desde WhatsApp (número: {}, archivo: {})", from, filename);
        } catch (Exception e) {
            log.error("Error al procesar comprobante de WhatsApp (número: {}, mediaId: {}): {}", from, mediaId, e.getMessage(), e);
        }
    }

    private byte[] descargarMedia(String mediaId) {
        String metadataJson = restClient.get()
                .uri(GRAPH_API + mediaId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(String.class);

        String mediaUrl;
        try {
            JsonNode node = objectMapper.readTree(metadataJson);
            mediaUrl = node.get("url").asText();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo obtener la URL de descarga del media " + mediaId, e);
        }

        return restClient.get()
                .uri(mediaUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(byte[].class);
    }

    private String extensionDeMimeType(String mimeType) {
        if (mimeType == null) return "jpg";
        return switch (mimeType) {
            case "image/png"       -> "png";
            case "image/gif"       -> "gif";
            case "image/webp"      -> "webp";
            case "application/pdf" -> "pdf";
            default                -> "jpg";
        };
    }
}
