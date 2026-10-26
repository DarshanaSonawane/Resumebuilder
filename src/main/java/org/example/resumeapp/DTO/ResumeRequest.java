package org.example.resumeapp.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ResumeRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be at most 100 characters")
    private String title;

    @Size(max = 255, message = "Thumbnail link is too long")
    private String thumbnailLink;

    @Valid
    private Template template;

    @Valid
    private ProfileInfo profileInfo;

    @Valid
    private ContactInfo contactInfo;

    @Valid
    @Size(max = 20, message = "At most 20 work experiences allowed")
    private List<WorkExperience> workExperiences = new ArrayList<>();

    @Valid
    @Size(max = 10, message = "At most 10 education entries allowed")
    private List<Education> educations = new ArrayList<>();

    @Valid
    @Size(max = 30, message = "At most 30 skills allowed")
    private List<Skill> skills = new ArrayList<>();

    @Valid
    @Size(max = 20, message = "At most 20 projects allowed")
    private List<Project> projects = new ArrayList<>();

    @Valid
    @Size(max = 20, message = "At most 20 certifications allowed")
    private List<Certification> certifications = new ArrayList<>();

    @Valid
    @Size(max = 10, message = "At most 10 languages allowed")
    private List<Language> languages = new ArrayList<>();

    @Size(max = 20, message = "At most 20 interests allowed")
    private List<@NotBlank(message = "Interest cannot be blank")
    @Size(max = 255, message = "Interest is too long") String> interests = new ArrayList<>();

    // ---------- Nested sections ----------

    @Data
    public static class Template {
        @Size(max = 50, message = "Theme must be at most 50 characters")
        private String theme;

        @Size(max = 10, message = "At most 10 colors allowed")
        private List<@NotBlank(message = "Color cannot be blank")
        @Size(max = 30, message = "Color value is too long") String> colorPalette = new ArrayList<>();
    }

    @Data
    public static class ProfileInfo {
        @Size(max = 255, message = "Profile preview URL is too long")
        private String profilePreviewUrl;

        @Size(max = 100, message = "Full name must be at most 100 characters")
        private String fullName;

        @Size(max = 100, message = "Designation must be at most 100 characters")
        private String designation;

        @Size(max = 2000, message = "Summary must be at most 2000 characters")
        private String summary;
    }

    @Data
    public static class ContactInfo {
        @Email(message = "Enter a valid email address")
        @Size(max = 255, message = "Email is too long")
        private String email;

        @Size(max = 30, message = "Phone must be at most 30 characters")
        private String phone;

        @Size(max = 100, message = "Location must be at most 100 characters")
        private String location;

        @Size(max = 255, message = "LinkedIn link is too long")
        private String linkedIn;

        @Size(max = 255, message = "GitHub link is too long")
        private String github;

        @Size(max = 255, message = "Website link is too long")
        private String website;
    }

    @Data
    public static class WorkExperience {
        @NotBlank(message = "Company is required")
        @Size(max = 255, message = "Company is too long")
        private String company;

        @NotBlank(message = "Role is required")
        @Size(max = 255, message = "Role is too long")
        private String role;

        @Size(max = 50, message = "Start date is too long")
        private String startDate;

        @Size(max = 50, message = "End date is too long")
        private String endDate;

        @Size(max = 2000, message = "Description must be at most 2000 characters")
        private String description;
    }

    @Data
    public static class Education {
        @NotBlank(message = "Degree is required")
        @Size(max = 255, message = "Degree is too long")
        private String degree;

        @NotBlank(message = "Institution is required")
        @Size(max = 255, message = "Institution is too long")
        private String institution;

        @Size(max = 50, message = "Start date is too long")
        private String startDate;

        @Size(max = 50, message = "End date is too long")
        private String endDate;
    }

    @Data
    public static class Skill {
        @NotBlank(message = "Skill name is required")
        @Size(max = 100, message = "Skill name is too long")
        private String name;

        @Min(value = 0, message = "Progress must be between 0 and 100")
        @Max(value = 100, message = "Progress must be between 0 and 100")
        private Integer progress;
    }

    @Data
    public static class Project {
        @NotBlank(message = "Project title is required")
        @Size(max = 255, message = "Project title is too long")
        private String title;

        @Size(max = 2000, message = "Description must be at most 2000 characters")
        private String description;

        @Size(max = 255, message = "GitHub link is too long")
        private String github;

        @Size(max = 255, message = "Live demo link is too long")
        private String liveDemo;
    }

    @Data
    public static class Certification {
        @NotBlank(message = "Certification title is required")
        @Size(max = 255, message = "Certification title is too long")
        private String title;

        @Size(max = 255, message = "Issuer is too long")
        private String issuer;

        @Size(max = 10, message = "Year is too long")
        private String year;
    }

    @Data
    public static class Language {
        @NotBlank(message = "Language name is required")
        @Size(max = 100, message = "Language name is too long")
        private String name;

        @Min(value = 0, message = "Progress must be between 0 and 100")
        @Max(value = 100, message = "Progress must be between 0 and 100")
        private Integer progress;
    }
}