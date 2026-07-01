package com.careerpilot.careerpilot.career.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GapAnalysisResponse {

    private Long id;
    private String targetRole;
    private int overallMatch;
    private String summary;
    private List<StrongSkillItem> strongSkills;
    private List<MissingSkillItem> missingSkills;
    private List<SkillToImproveItem> skillsToImprove;
    private LocalDateTime analyzedAt;
}
