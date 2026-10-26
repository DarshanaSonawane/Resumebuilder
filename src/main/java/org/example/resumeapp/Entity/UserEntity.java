package org.example.resumeapp.Entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // ... rest of the fields unchanged


    private String name;
    private String email;
    private String password;
    private String profileImageUrl;
    @Builder.Default
    private String subscription = "basic";

    @Builder.Default
    private boolean emailVerified = false;

    private String verificationToken;
    private LocalDateTime verificationExpires;
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}


