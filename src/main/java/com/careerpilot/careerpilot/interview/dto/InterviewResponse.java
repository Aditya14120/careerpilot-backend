package com.careerpilot.careerpilot.interview.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InterviewResponse {

    private Long id;
    private String targetRole;
    private int totalQuestions;
    private List<InterviewQuestion> questions;
    private LocalDateTime generatedAt;
}
