package com.careerpilot.careerpilot.skill.service.impl;

import com.careerpilot.careerpilot.ai.GeminiClient;
import com.careerpilot.careerpilot.exception.ResumeNotFoundException;
import com.careerpilot.careerpilot.resume.entity.Resume;
import com.careerpilot.careerpilot.resume.repository.ResumeRepository;
import com.careerpilot.careerpilot.skill.dto.SkillDto;
import com.careerpilot.careerpilot.skill.dto.SkillExtractionResponse;
import com.careerpilot.careerpilot.skill.entity.ExtractedSkill;
import com.careerpilot.careerpilot.skill.repository.ExtractedSkillRepository;
import com.careerpilot.careerpilot.skill.service.SkillExtractionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillExtractionServiceImpl implements SkillExtractionService {

    private final ResumeRepository resumeRepository;
    private final ExtractedSkillRepository extractedSkillRepository;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public SkillExtractionResponse extractSkills(Long resumeId, String userEmail) {
        Resume resume = getResumeAndVerifyOwner(resumeId, userEmail);

        if (resume.getExtractedText() == null || resume.getExtractedText().isBlank()) {
            throw new IllegalArgumentException("Resume text has not been extracted yet. Please re-upload the resume.");
        }

        String prompt = buildPrompt(resume.getExtractedText());
        String rawResponse = geminiClient.generate(prompt);
        List<SkillDto> skills = parseSkills(rawResponse);

        extractedSkillRepository.deleteByResumeId(resumeId);

        List<ExtractedSkill> entities = skills.stream()
                .map(dto -> ExtractedSkill.builder()
                        .resume(resume)
                        .name(dto.getName())
                        .category(dto.getCategory())
                        .proficiencyLevel(dto.getProficiencyLevel())
                        .extractedAt(LocalDateTime.now())
                        .build())
                .toList();

        extractedSkillRepository.saveAll(entities);

        return SkillExtractionResponse.builder()
                .resumeId(resumeId)
                .skills(skills)
                .totalCount(skills.size())
                .message("Skills extracted successfully")
                .build();
    }

    @Override
    public SkillExtractionResponse getSkillsByResume(Long resumeId, String userEmail) {
        getResumeAndVerifyOwner(resumeId, userEmail);

        List<SkillDto> skills = extractedSkillRepository.findByResumeIdOrderByNameAsc(resumeId)
                .stream()
                .map(s -> new SkillDto(s.getName(), s.getCategory(), s.getProficiencyLevel()))
                .toList();

        return SkillExtractionResponse.builder()
                .resumeId(resumeId)
                .skills(skills)
                .totalCount(skills.size())
                .message("Skills fetched successfully")
                .build();
    }

    private Resume getResumeAndVerifyOwner(Long resumeId, String userEmail) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResumeNotFoundException(resumeId));

        if (!resume.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("You do not have access to this resume");
        }

        return resume;
    }

    private String buildPrompt(String resumeText) {
        return """
                You are an expert technical recruiter and resume analyst with 15+ years of experience \
                evaluating candidates across all technology domains.

                TASK: Analyze the resume below and extract every skill — technical and professional — \
                mentioned explicitly or implied through project descriptions, job responsibilities, \
                and achievements.

                EXTRACTION RULES:
                1. Normalize all skill names to their official canonical form.
                   Examples: "JS" → "JavaScript", "Postgres" → "PostgreSQL", "React.js" → "React", \
                "K8s" → "Kubernetes", "AWS Lambda" → "AWS Lambda" (keep specific AWS services distinct)
                2. Extract skills from ALL sections: skills list, work experience, projects, \
                education, certifications, and achievements
                3. Remove duplicates — each skill must appear exactly once
                4. Infer proficiency level from context clues:
                   - "Expert" → led teams on this skill, 5+ years, or described as expert/architect
                   - "Advanced" → 3–5 years, senior-level usage, complex implementations
                   - "Intermediate" → 1–3 years, regular usage in projects
                   - "Beginner" → mentioned briefly, coursework only, or less than 1 year
                5. Do not fabricate skills that are not in the resume

                CATEGORIES (use exactly as written):
                Programming Language | Framework | Database | Cloud | DevOps Tool | \
                Testing | Data Science | Mobile | Security | Soft Skill | Other

                OUTPUT FORMAT — return ONLY a valid JSON array, nothing else. \
                No explanation, no markdown, no code fences, no trailing text:
                [
                  {"name":"Java","category":"Programming Language","proficiencyLevel":"Advanced"},
                  {"name":"Spring Boot","category":"Framework","proficiencyLevel":"Intermediate"},
                  {"name":"PostgreSQL","category":"Database","proficiencyLevel":"Intermediate"},
                  {"name":"Problem Solving","category":"Soft Skill","proficiencyLevel":"Advanced"}
                ]

                RESUME:
                """ + resumeText;
    }

    private List<SkillDto> parseSkills(String raw) {
        String cleaned = raw.trim()
                .replaceAll("(?s)```json\\s*", "")
                .replaceAll("(?s)```\\s*", "")
                .trim();

        try {
            return objectMapper.readValue(cleaned, new TypeReference<List<SkillDto>>() {});
        } catch (Exception e) {
            log.error("Failed to parse Gemini skill response: {}", cleaned, e);
            throw new RuntimeException("Gemini returned an unreadable response. Please try again.");
        }
    }
}
