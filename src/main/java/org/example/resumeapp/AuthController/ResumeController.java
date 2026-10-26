package org.example.resumeapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.resumeapp.DTO.ApplyTemplateRequest;
import org.example.resumeapp.DTO.ResumeRequest;
import org.example.resumeapp.DTO.ResumeResponse;
import org.example.resumeapp.DTO.ResumeSummaryResponse;
import org.example.resumeapp.service.FileUploadService;
import org.example.resumeapp.service.ResumeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;
private  final FileUploadService fileUploadService;
    // imports: org.springframework.http.MediaType, org.springframework.web.multipart.MultipartFile

    @PutMapping(value = "/{id}/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeResponse> uploadResumeImage(
            Authentication authentication,
            @PathVariable String id,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        log.info("Inside ResumeController: uploadResumeImage()");
        return ResponseEntity.ok(
                fileUploadService.uploadResumeImage(authentication.getName(), id, thumbnail, profileImage));
    }

    @PostMapping
    public ResponseEntity<ResumeResponse> create(
            Authentication authentication,
            @Valid @RequestBody ResumeRequest request) {
        log.info("Inside ResumeController: create()");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resumeService.create(authentication.getName(), request));
    }

    @GetMapping
    public ResponseEntity<List<ResumeSummaryResponse>> list(Authentication authentication) {
        log.info("Inside ResumeController: list()");
        return ResponseEntity.ok(resumeService.list(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> get(
            Authentication authentication,
            @PathVariable String id) {
        log.info("Inside ResumeController: get()");
        return ResponseEntity.ok(resumeService.get(authentication.getName(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResumeResponse> update(
            Authentication authentication,
            @PathVariable String id,
            @Valid @RequestBody ResumeRequest request) {
        log.info("Inside ResumeController: update()");
        return ResponseEntity.ok(resumeService.update(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(
            Authentication authentication,
            @PathVariable String id) {
        log.info("Inside ResumeController: delete()");
        resumeService.delete(authentication.getName(), id);
        return ResponseEntity.ok(Map.of("message", "Resume deleted successfully"));
    }

    @PutMapping("/{id}/template")
    public ResponseEntity<ResumeResponse> applyTemplate(
            Authentication authentication,
            @PathVariable String id,
            @Valid @RequestBody ApplyTemplateRequest request) {
        log.info("Inside ResumeController: applyTemplate()");
        return ResponseEntity.ok(resumeService.applyTemplate(authentication.getName(), id, request));
    }
}