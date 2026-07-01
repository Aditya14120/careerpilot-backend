package com.careerpilot.careerpilot.resume.dto;

import com.careerpilot.careerpilot.resume.entity.ResumeStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ResumeUploadResponse {

    private Long id;
    private String originalFileName;
    private ResumeStatus status;
    private String message;
    private LocalDateTime uploadedAt;
}
