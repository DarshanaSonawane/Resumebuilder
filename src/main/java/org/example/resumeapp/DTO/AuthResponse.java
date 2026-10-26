package org.example.resumeapp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String id;
    private String name;
    private String email;
    private String profileImageUrl;
    private String subscription;
    private boolean emailVerified;
    private LocalDateTime createdAt;

    private String token; // JWT, set on login
}

