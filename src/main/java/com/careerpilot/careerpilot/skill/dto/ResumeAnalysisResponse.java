package com.careerpilot.careerpilot.skill.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ResumeAnalysisResponse {
    private Long resumeId;
    private int overallScore;
    private int atsScore;
    private String summary;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> suggestions;
    private List<String> missingSections;
    private List<String> keywordSuggestions;
}
