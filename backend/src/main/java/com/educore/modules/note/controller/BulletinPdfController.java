package com.educore.modules.note.controller;

import com.educore.security.UtilisateurPrincipal;
import com.educore.modules.note.service.BulletinPdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/bulletins")
public class BulletinPdfController {

    private final BulletinPdfService bulletinPdfService;

    public BulletinPdfController(BulletinPdfService bulletinPdfService) {
        this.bulletinPdfService = bulletinPdfService;
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> genererPdf(
            @PathVariable Long id,
            @AuthenticationPrincipal UtilisateurPrincipal principal) throws IOException {
        
        byte[] pdfBytes = bulletinPdfService.genererPdf(principal.etablissementId(), id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "bulletin_" + id + ".pdf");
        headers.setContentLength(pdfBytes.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
