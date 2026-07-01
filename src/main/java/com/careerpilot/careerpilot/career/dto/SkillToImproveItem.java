package com.careerpilot.careerpilot.career.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkillToImproveItem {

    private String name;
    private String currentLevel;
    private String targetLevel;
    private String suggestion;
}
