package org.example.resumeapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.resumeapp.DTO.ResumeResponse;
import org.example.resumeapp.Entity.Resume;
import org.example.resumeapp.Entity.UserEntity;
import org.example.resumeapp.Exception.InvalidFileException;
import org.example.resumeapp.Exception.ResourceNotFoundException;
import org.example.resumeapp.Repository.ResumeRepository;
import org.example.resumeapp.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final ImageUploadService imageUploadService;
    private final ResumeService resumeService;   // reused for the entity -> response mapping

    @Transactional
    public ResumeResponse uploadResumeImage(String email, String resumeId,
                                            MultipartFile thumbnail, MultipartFile profileImage) {
        log.info("Inside FileUploadService: uploadResumeImage() resume {} for {}", resumeId, email);

        boolean hasThumbnail = hasFile(thumbnail);
        boolean hasProfileImage = hasFile(profileImage);
        if (!hasThumbnail && !hasProfileImage) {
            throw new InvalidFileException("Upload at least one image: thumbnail or profileImage");
        }

        // 1. current profile = the logged-in user
        UserEntity profile = getCurrentProfile(email);

        // 2. existing resume, only if it belongs to this profile (nothing goes to Cloudinary before this)
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        // 3. upload the images and 4. set the URLs on the resume
        String basePath = "resume-builder/resumes/" + resume.getId();

        if (hasThumbnail) {
            resume.setThumbnailLink(imageUploadService.uploadImage(thumbnail, basePath + "/thumbnail"));
        }
        if (hasProfileImage) {
            if (resume.getProfileInfo() == null) {
                resume.setProfileInfo(new Resume.ProfileInfo());
            }
            resume.getProfileInfo().setProfilePreviewUrl(
                    imageUploadService.uploadImage(profileImage, basePath + "/profile"));
        }

        // 5. save the details to the database
        Resume saved = resumeRepository.saveAndFlush(resume);

        // 6. return the result
        return resumeService.toResponse(saved);
    }

    private UserEntity getCurrentProfile(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }
}