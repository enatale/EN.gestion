package com.remar.EN.gestion.controller;

import com.remar.EN.gestion.dto.WhatsAppWebhookPayload;
import com.remar.EN.gestion.service.WhatsAppWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/whatsapp/webhook")
@RequiredArgsConstructor
public class WhatsAppWebhookController {

    @Value("${whatsapp.verify-token}")
    private String verifyToken;

    private final WhatsAppWebhookService whatsAppWebhookService;

    @GetMapping
    public ResponseEntity<String> verificar(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(403).build();
    }

    @PostMapping
    public ResponseEntity<Void> recibir(@RequestBody WhatsAppWebhookPayload payload) {
        whatsAppWebhookService.procesarMensaje(payload);
        // Meta requiere siempre HTTP 200, de lo contrario reintenta el envío
        return ResponseEntity.ok().build();
    }
}
