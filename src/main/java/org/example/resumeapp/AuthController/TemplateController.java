package org.example.resumeapp.controller;

import lombok.RequiredArgsConstructor;
import org.example.resumeapp.DTO.TemplateResponse;
import org.example.resumeapp.service.TemplateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping
    public ResponseEntity<List<TemplateResponse>> getTemplates() {
        return ResponseEntity.ok(templateService.getAll());
    }
}