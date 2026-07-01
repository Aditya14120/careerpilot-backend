package com.careerpilot.careerpilot.skill.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillDto {

    private String name;
    private String category;
    private String proficiencyLevel;
}
