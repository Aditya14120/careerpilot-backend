package com.careerpilot.careerpilot.career.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MissingSkillItem {

    private String name;
    private String priority;      // High | Medium | Low
    private String suggestion;
}
