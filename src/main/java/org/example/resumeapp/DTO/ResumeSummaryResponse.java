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
public class ResumeSummaryResponse {

    private String id;
    private String title;
    private String thumbnailLink;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}