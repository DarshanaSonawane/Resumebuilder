package org.example.resumeapp.DTO;

import java.util.List;

public record TemplateResponse(String id, String name, String description, List<List<String>> palettes) {

}