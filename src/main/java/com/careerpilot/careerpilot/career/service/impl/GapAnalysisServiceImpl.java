package com.careerpilot.careerpilot.career.service.impl;

import com.careerpilot.careerpilot.ai.GeminiClient;
import com.careerpilot.careerpilot.auth.entity.User;
import com.careerpilot.careerpilot.auth.repository.UserRepository;
import com.careerpilot.careerpilot.career.dto.GapAnalysisRequest;
import com.careerpilot.careerpilot.career.dto.GapAnalysisResponse;
import com.careerpilot.careerpilot.career.entity.GapAnalysis;
import com.careerpilot.careerpilot.career.repository.GapAnalysisRepository;
import com.careerpilot.careerpilot.career.service.GapAnalysisService;
import com.careerpilot.careerpilot.exception.ResumeNotFoundException;
import com.careerpilot.careerpilot.resume.entity.Resume;
import com.careerpilot.careerpilot.resume.repository.ResumeRepository;
import com.careerpilot.careerpilot.skill.dto.SkillDto;
import com.careerpilot.careerpilot.skill.repository.ExtractedSkillRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GapAnalysisServiceImpl implements GapAnalysisService {

    private final GapAnalysisRepository gapAnalysisRepository;
    private final ResumeRepository resumeRepository;
    private final ExtractedSkillRepository extractedSkillRepository;
    private final UserRepository userRepository;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public GapAnalysisResponse analyze(GapAnalysisRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Resume resume = resumeRepository.findById(request.getResumeId())
                .orElseThrow(() -> new ResumeNotFoundException(request.getResumeId()));

        if (!resume.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("You do not have access to this resume");
        }

        List<SkillDto> skills = extractedSkillRepository
                .findByResumeIdOrderByNameAsc(request.getResumeId())
                .stream()
                .map(s -> new SkillDto(s.getName(), s.getCategory(), s.getProficiencyLevel()))
                .toList();

        if (skills.isEmpty()) {
            throw new IllegalArgumentException(
                    "No skills found for this resume. Please run skill extraction first.");
        }

        String prompt = buildPrompt(skills, request.getTargetRole());
        String rawResponse = geminiClient.generate(prompt);

        String cleanedJson = rawResponse.trim()
                .replaceAll("(?s)```json\\s*", "")
                .replaceAll("(?s)```\\s*", "")
                .trim();

        GapAnalysisResponse parsed = parseResponse(cleanedJson);

        GapAnalysis entity = GapAnalysis.builder()
                .user(user)
                .resumeId(request.getResumeId())
                .targetRole(request.getTargetRole())
                .overallMatch(parsed.getOverallMatch())
                .resultJson(cleanedJson)
                .build();

        GapAnalysis saved = gapAnalysisRepository.save(entity);
        parsed.setId(saved.getId());
        parsed.setAnalyzedAt(saved.getAnalyzedAt());

        return parsed;
    }

    @Override
    public List<GapAnalysisResponse> getMyAnalyses(String userEmail) {
        return gapAnalysisRepository.findByUser_EmailOrderByAnalyzedAtDesc(userEmail)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private GapAnalysisResponse toResponse(GapAnalysis entity) {
        try {
            GapAnalysisResponse response = objectMapper.readValue(
                    entity.getResultJson(), GapAnalysisResponse.class);
            response.setId(entity.getId());
            response.setAnalyzedAt(entity.getAnalyzedAt());
            return response;
        } catch (Exception e) {
            log.error("Failed to deserialize gap analysis id={}", entity.getId(), e);
            GapAnalysisResponse fallback = new GapAnalysisResponse();
            fallback.setId(entity.getId());
            fallback.setTargetRole(entity.getTargetRole());
            fallback.setOverallMatch(entity.getOverallMatch());
            fallback.setAnalyzedAt(entity.getAnalyzedAt());
            return fallback;
        }
    }

    private String buildPrompt(List<SkillDto> skills, String targetRole) {
        String skillList = skills.stream()
                .map(s -> "  - " + s.getName()
                        + " | " + s.getCategory()
                        + " | " + (s.getProficiencyLevel() != null ? s.getProficiencyLevel() : "Unknown"))
                .collect(Collectors.joining("\n"));

        return "You are a senior technical recruiter and career coach with deep expertise in hiring for "
                + "software engineering roles. You are evaluating a candidate for the role described below.\n\n"
                + "CANDIDATE SKILLS (format: Skill Name | Category | Proficiency Level):\n"
                + skillList + "\n\n"
                + "TARGET ROLE: " + targetRole + "\n\n"
                + "TASK: Perform a thorough, honest, and actionable skill gap analysis.\n\n"
                + "INSTRUCTIONS:\n"
                + "1. strongSkills — list skills the candidate has that are directly valuable for this role. "
                + "For each, write one sentence explaining WHY it matters for this specific role.\n"
                + "2. missingSkills — list skills important for this role that are completely absent. "
                + "Only include skills genuinely expected (not optional extras). "
                + "Priority: High = must-have to get hired, Medium = important, Low = nice-to-have. "
                + "Include a concrete first step the candidate should take.\n"
                + "3. skillsToImprove — skills the candidate has but below the expected level for this role. "
                + "State current level, required target level, and a specific improvement action.\n"
                + "4. overallMatch — integer 0-100 reflecting job-readiness for this role. "
                + "Be realistic: 90+ means near-perfect fit, below 40 means major gaps.\n"
                + "5. summary — exactly 3-4 sentences: highlight the strongest asset, name the biggest gap, "
                + "and give the single highest-impact action the candidate should take right now.\n\n"
                + "Return ONLY a valid JSON object. No markdown, no explanation, no code fences, no extra text:\n"
                + "{\n"
                + "  \"targetRole\": \"" + targetRole + "\",\n"
                + "  \"overallMatch\": 72,\n"
                + "  \"summary\": \"Your summary here.\",\n"
                + "  \"strongSkills\": [\n"
                + "    {\"name\": \"Java\", \"relevance\": \"Core language required for most backend roles\"}\n"
                + "  ],\n"
                + "  \"missingSkills\": [\n"
                + "    {\"name\": \"Docker\", \"priority\": \"High\", "
                + "\"suggestion\": \"Start by containerizing your existing Spring Boot projects\"}\n"
                + "  ],\n"
                + "  \"skillsToImprove\": [\n"
                + "    {\"name\": \"Spring Boot\", \"currentLevel\": \"Intermediate\", "
                + "\"targetLevel\": \"Advanced\", "
                + "\"suggestion\": \"Explore Spring Cloud, circuit breakers, and event-driven patterns\"}\n"
                + "  ]\n"
                + "}";
    }

    private GapAnalysisResponse parseResponse(String cleanedJson) {
        try {
            return objectMapper.readValue(cleanedJson, GapAnalysisResponse.class);
        } catch (Exception e) {
            log.error("Failed to parse Gemini gap analysis response: {}", cleanedJson, e);
            throw new RuntimeException("Gemini returned an unreadable response. Please try again.");
        }
    }
}
