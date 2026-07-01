package com.careerpilot.careerpilot.career.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StrongSkillItem {

    private String name;
    private String relevance;
}
