package com.example.eventsphere.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
public class FileUploadController {

    @Value("${app.upload-dir:uploads/documents}")
    private String uploadDir;

    private static final Set<String> ALLOWED_TYPES = Set.of("image/png", "image/jpeg", "application/pdf");
    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10MB

    // POST /auth/upload-document  (used by verified visitor registration)
    @PostMapping("/auth/upload-document")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file) {
        return handleUpload(file);
    }

    // POST /uploads/document  (alias used by frontend)
    @PostMapping("/uploads/document")
    public ResponseEntity<?> uploadDocumentAlias(@RequestParam("file") MultipartFile file) {
        return handleUpload(file);
    }

    private ResponseEntity<?> handleUpload(MultipartFile file) {
        try {
            if (file == null || file.isEmpty())
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Aucun fichier reçu."));

            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_TYPES.contains(contentType))
                return ResponseEntity.badRequest().body(Map.of("success", false,
                        "message", "Format non supporté. Utilisez PNG, JPG ou PDF."));

            if (file.getSize() > MAX_SIZE)
                return ResponseEntity.badRequest().body(Map.of("success", false,
                        "message", "Fichier trop grand. Maximum 10MB."));

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            String extension = getExtension(file.getOriginalFilename());
            String fileName = "cin_" + UUID.randomUUID() + "." + extension;
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String savedPath = uploadDir + "/" + fileName;
            return ResponseEntity.ok(Map.of(
                "success", true,
                "filePath", savedPath,
                "path", savedPath,
                "filename", fileName,
                "message", "Carte nationale uploadée avec succès."
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false,
                    "message", "Erreur upload : " + e.getMessage()));
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
