package org.example.resumeapp.AuthController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.resumeapp.DTO.ChangePasswordRequest;
import org.example.resumeapp.DTO.ProfileResponse;
import org.example.resumeapp.DTO.UpdateProfileRequest;
import org.example.resumeapp.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(Authentication authentication) {
        return ResponseEntity.ok(profileService.getProfile(authentication.getName()));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(authentication.getName(), request));
    }

    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        profileService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}