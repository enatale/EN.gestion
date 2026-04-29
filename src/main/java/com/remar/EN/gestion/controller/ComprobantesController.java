package com.remar.EN.gestion.controller;

import com.remar.EN.gestion.dto.ComprobantesResponseDTO;
import com.remar.EN.gestion.service.ComprobantesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/comprobantes")
@RequiredArgsConstructor
public class ComprobantesController {

    private final ComprobantesService service;

    @PostMapping("/leer")
    public ResponseEntity<ComprobantesResponseDTO> leer(
            @RequestParam("archivo") MultipartFile archivo) throws Exception {
        if (archivo.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(service.leer(archivo));
    }
}
