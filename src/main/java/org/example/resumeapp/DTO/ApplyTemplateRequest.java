package org.example.resumeapp.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ApplyTemplateRequest {

    @NotBlank(message = "Theme is required")
    @Size(max = 50, message = "Theme must be at most 50 characters")
    private String theme;

    // Optional: when empty, the theme's first default palette is used
    @Size(max = 10, message = "At most 10 colors allowed")
    private List<@NotBlank(message = "Color cannot be blank")
    @Size(max = 30, message = "Color value is too long") String> colorPalette = new ArrayList<>();
}