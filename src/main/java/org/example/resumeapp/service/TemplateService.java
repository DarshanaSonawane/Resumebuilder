package org.example.resumeapp.service;

import org.example.resumeapp.DTO.TemplateResponse;
import org.example.resumeapp.Entity.Resume;
import org.example.resumeapp.Exception.InvalidTemplateException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class TemplateService {

    public static final String DEFAULT_THEME = "classic";
    private static final int MAX_PALETTE_SIZE = 10;
    private static final Pattern HEX_COLOR = Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");

    // The ids must match the template names your frontend renders
    private static final List<TemplateResponse> CATALOG = List.of(
            new TemplateResponse("classic", "Classic",
                    "Traditional single-column layout",
                    List.of(List.of("#1F2937", "#4B5563", "#F3F4F6"),
                            List.of("#0F172A", "#334155", "#F8FAFC"))),
            new TemplateResponse("modern", "Modern",
                    "Two-column layout with a coloured sidebar",
                    List.of(List.of("#2563EB", "#1E40AF", "#EFF6FF"),
                            List.of("#059669", "#065F46", "#ECFDF5"),
                            List.of("#7C3AED", "#5B21B6", "#F5F3FF"))),
            new TemplateResponse("minimal", "Minimal",
                    "Clean, text-focused layout with lots of white space",
                    List.of(List.of("#111827", "#6B7280", "#FFFFFF"),
                            List.of("#374151", "#9CA3AF", "#FAFAFA"))),
            new TemplateResponse("creative", "Creative",
                    "Bold headings and strong accent colours",
                    List.of(List.of("#DB2777", "#9D174D", "#FDF2F8"),
                            List.of("#EA580C", "#9A3412", "#FFF7ED")))
    );

    public List<TemplateResponse> getAll() {
        return CATALOG;
    }

    public Resume.Template defaultTemplate() {
        return resolve(DEFAULT_THEME, null);
    }

    // Validates the theme and colours; returns the entity object ready to save
    public Resume.Template resolve(String theme, List<String> colorPalette) {
        TemplateResponse template = find(theme).orElseThrow(() ->
                new InvalidTemplateException("Unknown template. Available: " + availableIds()));

        List<String> palette = (colorPalette == null || colorPalette.isEmpty())
                ? new ArrayList<>(template.palettes().get(0))
                : validatePalette(colorPalette);

        return Resume.Template.builder()
                .theme(template.id())
                .colorPalette(palette)
                .build();
    }

    private Optional<TemplateResponse> find(String theme) {
        if (theme == null) {
            return Optional.empty();
        }
        String id = theme.trim().toLowerCase(Locale.ROOT);
        return CATALOG.stream().filter(t -> t.id().equals(id)).findFirst();
    }

    private String availableIds() {
        return CATALOG.stream().map(TemplateResponse::id).collect(Collectors.joining(", "));
    }

    private List<String> validatePalette(List<String> colors) {
        if (colors.size() > MAX_PALETTE_SIZE) {
            throw new InvalidTemplateException("At most " + MAX_PALETTE_SIZE + " colors allowed");
        }
        List<String> result = new ArrayList<>();
        for (String color : colors) {
            String value = color == null ? "" : color.trim();
            if (!HEX_COLOR.matcher(value).matches()) {
                throw new InvalidTemplateException("Colors must be hex values like #1F2937");
            }
            result.add(value.toUpperCase(Locale.ROOT));
        }
        return result;
    }
}