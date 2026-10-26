
package org.example.resumeapp.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.resumeapp.Exception.ImageUploadException;
import org.example.resumeapp.Exception.InvalidFileException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    private final Cloudinary cloudinary;

    // Profile images: unchanged behaviour
    public String uploadImage(MultipartFile file) {
        return upload(file, ObjectUtils.asMap(
                "folder", "resume-builder/profile-images",
                "resource_type", "image"));
    }

    // Resume images: fixed publicId + overwrite, so re-uploading replaces instead of piling up copies
    public String uploadImage(MultipartFile file, String publicId) {
        return upload(file, ObjectUtils.asMap(
                "public_id", publicId,
                "resource_type", "image",
                "overwrite", true,
                "invalidate", true));
    }

    private String upload(MultipartFile file, Map<?, ?> options) {
        validate(file);
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), options);
            return result.get("secure_url").toString();
        } catch (Exception e) {
            log.error("Cloudinary upload failed: {}", e.getMessage());
            throw new ImageUploadException("Image upload failed, please try again");
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is required");
        }
        String type = file.getContentType();
        if (type == null || !ALLOWED_TYPES.contains(type)) {
            throw new InvalidFileException("Only JPG, PNG or WEBP images are allowed");
        }
    }
}