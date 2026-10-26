package org.example.resumeapp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeResponse {

    private String id;
    private String title;
    private String thumbnailLink;

    private ResumeRequest.Template template;
    private ResumeRequest.ProfileInfo profileInfo;
    private ResumeRequest.ContactInfo contactInfo;

    private List<ResumeRequest.WorkExperience> workExperiences;
    private List<ResumeRequest.Education> educations;
    private List<ResumeRequest.Skill> skills;
    private List<ResumeRequest.Project> projects;
    private List<ResumeRequest.Certification> certifications;
    private List<ResumeRequest.Language> languages;
    private List<String> interests;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}