package org.example.resumeapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.resumeapp.DTO.AuthResponse;
import org.example.resumeapp.DTO.LoginRequest;
import org.example.resumeapp.DTO.RegisterRequest;
import org.example.resumeapp.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Inside AuthController: register()");
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        log.info("Inside AuthController: verifyEmail()");
        authService.verifyEmail(token);
        return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}