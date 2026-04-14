package com.example.eventsphere.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class FileUploadController {

    // Dossier où les documents seront stockés sur le serveur
    @Value("${app.upload-dir:uploads/documents}")
    private String uploadDir;

    // ──────────────────────────────────────────────────────────
    // POST /auth/upload-document
    // Image 2 du front : Upload du document d'identité
    // pour le Visiteur Vérifié (Passport, CIN, Permis)
    //
    // Étapes frontend :
    //   1. L'utilisateur clique "Browse" ou drag & drop
    //   2. Le front appelle cet endpoint
    //   3. Reçoit { "filePath": "uploads/documents/xxx.jpg" }
    //   4. Utilise ce filePath dans /auth/register/verified
    // ──────────────────────────────────────────────────────────
    @PostMapping("/upload-document")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            // Vérifier le type de fichier (PNG, JPG, PDF uniquement - comme indiqué dans l'UI)
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("image/png")
                    && !contentType.equals("image/jpeg")
                    && !contentType.equals("application/pdf"))) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Format non supporté. Utilisez PNG, JPG ou PDF."
                ));
            }

            // Vérifier la taille (max 10MB comme indiqué dans l'UI)
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Fichier trop grand. Maximum 10MB."
                ));
            }

            // Créer le dossier si nécessaire
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Nom unique pour éviter les collisions
            String extension = getExtension(file.getOriginalFilename());
            String fileName = UUID.randomUUID().toString() + "." + extension;
            Path filePath = uploadPath.resolve(fileName);

            // Sauvegarder le fichier
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String savedPath = uploadDir + "/" + fileName;
            return ResponseEntity.ok(Map.of(
                "success", true,
                "filePath", savedPath,
                "message", "Document uploadé avec succès."
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Erreur lors de l'upload : " + e.getMessage()
            ));
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
