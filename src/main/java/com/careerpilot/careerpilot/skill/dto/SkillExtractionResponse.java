package com.careerpilot.careerpilot.skill.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SkillExtractionResponse {

    private Long resumeId;
    private List<SkillDto> skills;
    private int totalCount;
    private String message;
}
