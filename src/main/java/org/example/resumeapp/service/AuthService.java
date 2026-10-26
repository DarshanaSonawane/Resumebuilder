package org.example.resumeapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.resumeapp.DTO.AuthResponse;
import org.example.resumeapp.DTO.LoginRequest;
import org.example.resumeapp.DTO.RegisterRequest;
import org.example.resumeapp.Entity.UserEntity;
import org.example.resumeapp.Exception.InvalidTokenException;
import org.example.resumeapp.Exception.ResourceExistsException;
import org.example.resumeapp.Repository.UserRepository;
import org.example.resumeapp.SecurityConfig.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        log.info("Inside AuthService: register() for email {}", email);

        if (userRepository.existsByEmail(email)) {
            throw new ResourceExistsException("Email already registered");
        }

        UserEntity user = UserEntity.builder()
                .name(request.getName())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .profileImageUrl(request.getProfileImageUrl())
                .verificationToken(UUID.randomUUID().toString())
                .verificationExpires(LocalDateTime.now().plusHours(24))
                .build();

        UserEntity saved = userRepository.save(user);
        emailService.sendVerificationEmail(saved.getEmail(), saved.getName(), saved.getVerificationToken());

        return toResponse(saved);
    }

    private AuthResponse toResponse(UserEntity user) {
        return AuthResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .subscription(user.getSubscription())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public void verifyEmail(String token) {
        UserEntity user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

        if (user.getVerificationExpires().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Verification link has expired");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationExpires(null);
        userRepository.save(user);
    }

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        log.info("Inside AuthService: login() for email {}", email);

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword()));

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        AuthResponse response = toResponse(user);
        response.setToken(jwtService.generateToken(user.getEmail()));
        return response;
    }
}