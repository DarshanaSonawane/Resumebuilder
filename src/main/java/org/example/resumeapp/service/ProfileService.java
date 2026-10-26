package org.example.resumeapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.resumeapp.DTO.ChangePasswordRequest;
import org.example.resumeapp.DTO.ProfileResponse;
import org.example.resumeapp.DTO.UpdateProfileRequest;
import org.example.resumeapp.Entity.UserEntity;
import org.example.resumeapp.Exception.InvalidPasswordException;
import org.example.resumeapp.Exception.ResourceNotFoundException;
import org.example.resumeapp.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileResponse getProfile(String email) {
        log.info("Inside ProfileService: getProfile() for {}", email);
        return toResponse(findUser(email));
    }

    public ProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        log.info("Inside ProfileService: updateProfile() for {}", email);
        UserEntity user = findUser(email);

        user.setName(request.getName().trim());

        String imageUrl = request.getProfileImageUrl();
        user.setProfileImageUrl(imageUrl == null || imageUrl.isBlank() ? null : imageUrl.trim());

        return toResponse(userRepository.save(user));
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        log.info("Inside ProfileService: changePassword() for {}", email);
        UserEntity user = findUser(email);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new InvalidPasswordException("New password must be different from the current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private UserEntity findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ProfileResponse toResponse(UserEntity user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .subscription(user.getSubscription())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}