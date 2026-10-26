package org.example.resumeapp.DTO;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private String id;
    private String name;
    private String email;
    private String profileImageUrl;
    private String subscription;
    private boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}