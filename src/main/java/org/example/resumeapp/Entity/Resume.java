package org.example.resumeapp.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "resume", indexes = @Index(name = "idx_resume_user_id", columnList = "user_id"))
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    private String title;
    private String thumbnailLink;

    @Embedded
    private Template template;

    @Embedded
    private ProfileInfo profileInfo;

    @Embedded
    private ContactInfo contactInfo;

    @ElementCollection
    @CollectionTable(name = "resume_work_experience", joinColumns = @JoinColumn(name = "resume_id"))
    @Builder.Default
    private List<WorkExperience> workExperiences = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "resume_education", joinColumns = @JoinColumn(name = "resume_id"))
    @Builder.Default
    private List<Education> educations = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "resume_skill", joinColumns = @JoinColumn(name = "resume_id"))
    @Builder.Default
    private List<Skill> skills = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "resume_project", joinColumns = @JoinColumn(name = "resume_id"))
    @Builder.Default
    private List<Project> projects = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "resume_certification", joinColumns = @JoinColumn(name = "resume_id"))
    @Builder.Default
    private List<Certification> certifications = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "resume_language", joinColumns = @JoinColumn(name = "resume_id"))
    @Builder.Default
    private List<Language> languages = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "resume_interest", joinColumns = @JoinColumn(name = "resume_id"))
    @Column(name = "interest")
    @Builder.Default
    private List<String> interests = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ---------- Embedded (single) sections ----------

    // @Getter/@Setter instead of @Data: this one holds a lazy collection
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class Template {
        private String theme;

        @ElementCollection
        @CollectionTable(name = "resume_color_palette", joinColumns = @JoinColumn(name = "resume_id"))
        @Column(name = "color")
        @Builder.Default
        private List<String> colorPalette = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class ProfileInfo {
        private String profilePreviewUrl;
        private String fullName;
        private String designation;

        @Column(length = 2000)
        private String summary;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class ContactInfo {
        private String email;
        private String phone;
        private String location;
        private String linkedIn;
        private String github;
        private String website;
    }

    // ---------- Repeating sections (one row each) ----------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class WorkExperience {
        private String company;
        private String role;
        private String startDate;   // String so "Jan 2024" / "Present" both work
        private String endDate;

        @Column(length = 2000)
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class Education {
        private String degree;
        private String institution;
        private String startDate;
        private String endDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class Skill {
        private String name;
        private Integer progress;   // 0-100
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class Project {
        private String title;

        @Column(length = 2000)
        private String description;

        private String github;
        private String liveDemo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class Certification {
        private String title;
        private String issuer;

        @Column(name = "issued_year")   // "year" is a reserved word in some databases
        private String year;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class Language {
        private String name;
        private Integer progress;   // 0-100
    }
}