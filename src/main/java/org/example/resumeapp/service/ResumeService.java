package org.example.resumeapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.resumeapp.DTO.ApplyTemplateRequest;
import org.example.resumeapp.DTO.ResumeRequest;
import org.example.resumeapp.DTO.ResumeResponse;
import org.example.resumeapp.DTO.ResumeSummaryResponse;
import org.example.resumeapp.Entity.Resume;
import org.example.resumeapp.Entity.UserEntity;
import org.example.resumeapp.Exception.ResourceNotFoundException;
import org.example.resumeapp.Repository.ResumeRepository;
import org.example.resumeapp.Repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final TemplateService templateService;

    // ---------- CRUD ----------

    @Transactional
    public ResumeResponse create(String email, ResumeRequest request) {
        log.info("Inside ResumeService: create() for {}", email);
        Resume resume = Resume.builder().userId(currentUserId(email)).build();
        applyRequest(resume, request);
        return toResponse(resumeRepository.saveAndFlush(resume));
    }

    @Transactional(readOnly = true)
    public List<ResumeSummaryResponse> list(String email) {
        log.info("Inside ResumeService: list() for {}", email);
        return resumeRepository.findByUserIdOrderByUpdatedAtDesc(currentUserId(email))
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResumeResponse get(String email, String id) {
        log.info("Inside ResumeService: get() id {} for {}", id, email);
        return toResponse(findOwned(email, id));
    }

    @Transactional
    public ResumeResponse update(String email, String id, ResumeRequest request) {
        log.info("Inside ResumeService: update() id {} for {}", id, email);
        Resume resume = findOwned(email, id);
        applyRequest(resume, request);
        return toResponse(resumeRepository.saveAndFlush(resume));
    }

    @Transactional
    public void delete(String email, String id) {
        log.info("Inside ResumeService: delete() id {} for {}", id, email);
        resumeRepository.delete(findOwned(email, id));
    }

    // ---------- Template ----------

    @Transactional
    public ResumeResponse applyTemplate(String email, String id, ApplyTemplateRequest request) {
        log.info("Inside ResumeService: applyTemplate() id {} for {}", id, email);
        Resume resume = findOwned(email, id);
        resume.setTemplate(templateService.resolve(request.getTheme(), request.getColorPalette()));
        return toResponse(resumeRepository.saveAndFlush(resume));
    }

    // ---------- helpers ----------

    private String currentUserId(String email) {
        return userRepository.findByEmail(email)
                .map(UserEntity::getId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // Someone else's resume gives the same 404 as a missing one, so ids can't be probed
    private Resume findOwned(String email, String id) {
        return resumeRepository.findByIdAndUserId(id, currentUserId(email))
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
    }

    // Used by both create and update: full replacement of the editable fields.
    // thumbnailLink and profileInfo.profilePreviewUrl are NOT taken from the request:
    // only FileUploadService.uploadResumeImage() changes them.
    private void applyRequest(Resume resume, ResumeRequest request) {
        resume.setTitle(request.getTitle().trim());

        // Template: new resume -> default; update with no template sent -> keep the current one
        if (request.getTemplate() != null) {
            resume.setTemplate(templateService.resolve(
                    request.getTemplate().getTheme(), request.getTemplate().getColorPalette()));
        } else if (resume.getTemplate() == null) {
            resume.setTemplate(templateService.defaultTemplate());
        }

        // Keep the uploaded preview image URL, ignore whatever the client sent
        String existingPreviewUrl = resume.getProfileInfo() != null
                ? resume.getProfileInfo().getProfilePreviewUrl() : null;
        Resume.ProfileInfo profileInfo = mapOne(request.getProfileInfo(), Resume.ProfileInfo::new);
        profileInfo.setProfilePreviewUrl(existingPreviewUrl);
        resume.setProfileInfo(profileInfo);

        resume.setContactInfo(mapOne(request.getContactInfo(), Resume.ContactInfo::new));
        resume.setWorkExperiences(mapList(request.getWorkExperiences(), Resume.WorkExperience::new));
        resume.setEducations(mapList(request.getEducations(), Resume.Education::new));
        resume.setSkills(mapList(request.getSkills(), Resume.Skill::new));
        resume.setProjects(mapList(request.getProjects(), Resume.Project::new));
        resume.setCertifications(mapList(request.getCertifications(), Resume.Certification::new));
        resume.setLanguages(mapList(request.getLanguages(), Resume.Language::new));
        resume.setInterests(copyOf(request.getInterests()));
    }

    // Public because FileUploadService reuses it.
    // Must run inside a transaction: lazy collections are copied into plain lists here.
    public ResumeResponse toResponse(Resume resume) {
        return ResumeResponse.builder()
                .id(resume.getId())
                .title(resume.getTitle())
                .thumbnailLink(resume.getThumbnailLink())
                .template(toTemplateDto(resume.getTemplate()))
                .profileInfo(mapOne(resume.getProfileInfo(), ResumeRequest.ProfileInfo::new))
                .contactInfo(mapOne(resume.getContactInfo(), ResumeRequest.ContactInfo::new))
                .workExperiences(mapList(resume.getWorkExperiences(), ResumeRequest.WorkExperience::new))
                .educations(mapList(resume.getEducations(), ResumeRequest.Education::new))
                .skills(mapList(resume.getSkills(), ResumeRequest.Skill::new))
                .projects(mapList(resume.getProjects(), ResumeRequest.Project::new))
                .certifications(mapList(resume.getCertifications(), ResumeRequest.Certification::new))
                .languages(mapList(resume.getLanguages(), ResumeRequest.Language::new))
                .interests(copyOf(resume.getInterests()))
                .createdAt(resume.getCreatedAt())
                .updatedAt(resume.getUpdatedAt())
                .build();
    }

    private ResumeSummaryResponse toSummary(Resume resume) {
        return ResumeSummaryResponse.builder()
                .id(resume.getId())
                .title(resume.getTitle())
                .thumbnailLink(resume.getThumbnailLink())
                .createdAt(resume.getCreatedAt())
                .updatedAt(resume.getUpdatedAt())
                .build();
    }

    // Older resumes saved without a template are shown with the default one
    private ResumeRequest.Template toTemplateDto(Resume.Template source) {
        Resume.Template effective = source != null ? source : templateService.defaultTemplate();
        ResumeRequest.Template dto = new ResumeRequest.Template();
        dto.setTheme(effective.getTheme());
        dto.setColorPalette(copyOf(effective.getColorPalette()));
        return dto;
    }

    // Copies same-named properties; a missing section becomes an empty object
    private <S, T> T mapOne(S source, Supplier<T> factory) {
        T target = factory.get();
        if (source != null) {
            BeanUtils.copyProperties(source, target);
        }
        return target;
    }

    private <S, T> List<T> mapList(List<S> source, Supplier<T> factory) {
        List<T> result = new ArrayList<>();
        if (source != null) {
            for (S item : source) {
                T target = factory.get();
                BeanUtils.copyProperties(item, target);
                result.add(target);
            }
        }
        return result;
    }

    private <T> List<T> copyOf(List<T> source) {
        return source == null ? new ArrayList<>() : new ArrayList<>(source);
    }
}