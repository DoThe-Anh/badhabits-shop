package com.shop.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileUploadService {

    private final Path uploadRoot;

    public FileUploadService(@Value("${app.upload.dir:../uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    /**
     * Lưu file upload, trả về relative URL `/uploads/<random>.ext`.
     * Trả null nếu file rỗng.
     */
    public String save(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            Files.createDirectories(uploadRoot);
            String ext = extractExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + ext;
            Path dest = uploadRoot.resolve(filename).normalize();
            // Defense: dest phải nằm trong uploadRoot
            if (!dest.startsWith(uploadRoot)) {
                throw new IllegalStateException("Đường dẫn upload không hợp lệ");
            }
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Không lưu được file upload: " + e.getMessage(), e);
        }
    }

    private static String extractExtension(String original) {
        if (original == null) return "";
        int dot = original.lastIndexOf('.');
        if (dot < 0 || dot >= original.length() - 1) return "";
        String ext = original.substring(dot).toLowerCase();
        // Chỉ cho phép vài extension ảnh phổ biến
        return switch (ext) {
            case ".jpg", ".jpeg", ".png", ".webp", ".gif", ".avif" -> ext;
            default -> "";
        };
    }
}
