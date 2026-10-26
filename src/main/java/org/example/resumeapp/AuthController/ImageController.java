package org.example.resumeapp.AuthController;

import lombok.RequiredArgsConstructor;
import org.example.resumeapp.service.ImageUploadService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageUploadService imageUploadService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        String url = imageUploadService.uploadImage(file);
        return ResponseEntity.ok(Map.of("imageUrl", url));
    }
}