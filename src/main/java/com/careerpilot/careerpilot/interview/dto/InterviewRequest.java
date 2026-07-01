package com.careerpilot.careerpilot.interview.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class InterviewRequest {

    @NotNull(message = "Resume ID is required")
    private Long resumeId;

    @NotBlank(message = "Target role is required")
    private String targetRole;

    @Min(value = 5, message = "Minimum 5 questions")
    @Max(value = 20, message = "Maximum 20 questions")
    private int numberOfQuestions = 10;
}
