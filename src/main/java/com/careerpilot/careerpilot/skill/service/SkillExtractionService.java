package com.careerpilot.careerpilot.skill.service;

import com.careerpilot.careerpilot.skill.dto.ResumeAnalysisResponse;
import com.careerpilot.careerpilot.skill.dto.SkillExtractionResponse;

public interface SkillExtractionService {

    SkillExtractionResponse extractSkills(Long resumeId, String userEmail);

    SkillExtractionResponse getSkillsByResume(Long resumeId, String userEmail);

    ResumeAnalysisResponse analyzeResume(Long resumeId, String userEmail);
}
