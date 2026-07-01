package com.careerpilot.careerpilot.interview.service.impl;

import com.careerpilot.careerpilot.ai.GeminiClient;
import com.careerpilot.careerpilot.auth.entity.User;
import com.careerpilot.careerpilot.auth.repository.UserRepository;
import com.careerpilot.careerpilot.exception.ResumeNotFoundException;
import com.careerpilot.careerpilot.interview.dto.InterviewQuestion;
import com.careerpilot.careerpilot.interview.dto.InterviewRequest;
import com.careerpilot.careerpilot.interview.dto.InterviewResponse;
import com.careerpilot.careerpilot.interview.entity.InterviewSession;
import com.careerpilot.careerpilot.interview.repository.InterviewSessionRepository;
import com.careerpilot.careerpilot.interview.service.InterviewService;
import com.careerpilot.careerpilot.resume.repository.ResumeRepository;
import com.careerpilot.careerpilot.skill.dto.SkillDto;
import com.careerpilot.careerpilot.skill.repository.ExtractedSkillRepository;
import com.fasterxml.jackson.core.type.TypeReference;
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
public class InterviewServiceImpl implements InterviewService {

    private final InterviewSessionRepository sessionRepository;
    private final ResumeRepository resumeRepository;
    private final ExtractedSkillRepository extractedSkillRepository;
    private final UserRepository userRepository;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public InterviewResponse generate(InterviewRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        var resume = resumeRepository.findById(request.getResumeId())
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

        String prompt = buildPrompt(skills, request.getTargetRole(), request.getNumberOfQuestions());
        String rawResponse = geminiClient.generate(prompt);

        String cleanedJson = rawResponse.trim()
                .replaceAll("(?s)```json\\s*", "")
                .replaceAll("(?s)```\\s*", "")
                .trim();

        List<InterviewQuestion> questions = parseQuestions(cleanedJson);

        InterviewSession session = InterviewSession.builder()
                .user(user)
                .resumeId(request.getResumeId())
                .targetRole(request.getTargetRole())
                .totalQuestions(questions.size())
                .resultJson(cleanedJson)
                .build();

        InterviewSession saved = sessionRepository.save(session);

        InterviewResponse response = new InterviewResponse();
        response.setId(saved.getId());
        response.setTargetRole(saved.getTargetRole());
        response.setTotalQuestions(saved.getTotalQuestions());
        response.setQuestions(questions);
        response.setGeneratedAt(saved.getGeneratedAt());
        return response;
    }

    @Override
    public List<InterviewResponse> getMySessions(String userEmail) {
        return sessionRepository.findByUser_EmailOrderByGeneratedAtDesc(userEmail)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public InterviewResponse getSession(Long sessionId, String userEmail) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Interview session not found: " + sessionId));

        if (!session.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("You do not have access to this session");
        }

        return toResponse(session);
    }

    private InterviewResponse toResponse(InterviewSession session) {
        try {
            List<InterviewQuestion> questions = objectMapper.readValue(
                    session.getResultJson(), new TypeReference<List<InterviewQuestion>>() {});
            InterviewResponse response = new InterviewResponse();
            response.setId(session.getId());
            response.setTargetRole(session.getTargetRole());
            response.setTotalQuestions(session.getTotalQuestions());
            response.setQuestions(questions);
            response.setGeneratedAt(session.getGeneratedAt());
            return response;
        } catch (Exception e) {
            log.error("Failed to deserialize interview session id={}", session.getId(), e);
            InterviewResponse fallback = new InterviewResponse();
            fallback.setId(session.getId());
            fallback.setTargetRole(session.getTargetRole());
            fallback.setTotalQuestions(session.getTotalQuestions());
            fallback.setGeneratedAt(session.getGeneratedAt());
            return fallback;
        }
    }

    private String buildPrompt(List<SkillDto> skills, String targetRole, int numberOfQuestions) {
        int technical   = (int) Math.round(numberOfQuestions * 0.50);
        int behavioral  = (int) Math.round(numberOfQuestions * 0.30);
        int situational = numberOfQuestions - technical - behavioral;

        String skillList = skills.stream()
                .map(s -> s.getName() + " (" + s.getCategory()
                        + (s.getProficiencyLevel() != null ? ", " + s.getProficiencyLevel() : "") + ")")
                .collect(Collectors.joining(", "));

        return "You are a senior technical interviewer with 15+ years of experience hiring for "
                + targetRole + " roles at top technology companies.\n\n"
                + "CANDIDATE PROFILE:\n"
                + "Target Role: " + targetRole + "\n"
                + "Skills: " + skillList + "\n\n"
                + "TASK: Generate exactly " + numberOfQuestions + " interview questions tailored to this "
                + "candidate's skill set and the target role.\n\n"
                + "DISTRIBUTION:\n"
                + "- " + technical + " Technical questions (test depth of knowledge in their skills)\n"
                + "- " + behavioral + " Behavioral questions (past experience, teamwork, conflict resolution)\n"
                + "- " + situational + " Situational questions (hypothetical scenarios they may face in this role)\n\n"
                + "RULES:\n"
                + "1. Technical questions must be specific to the candidate's listed skills — no generic questions\n"
                + "2. Vary difficulty: roughly one-third Easy, one-third Medium, one-third Hard\n"
                + "3. Each hint must be 1-2 sentences: what a strong answer covers, NOT the full answer\n"
                + "4. Do not repeat similar questions\n"
                + "5. Behavioral and Situational questions should relate to the target role context\n\n"
                + "DIFFICULTY GUIDE:\n"
                + "- Easy: foundational concepts, definitions, basic usage\n"
                + "- Medium: design decisions, trade-offs, intermediate implementation\n"
                + "- Hard: system design, edge cases, deep architectural knowledge\n\n"
                + "Return ONLY a valid JSON array. No markdown, no explanation, no code fences:\n"
                + "[\n"
                + "  {\n"
                + "    \"question\": \"Explain the difference between @Component, @Service, and @Repository in Spring.\",\n"
                + "    \"type\": \"Technical\",\n"
                + "    \"difficulty\": \"Easy\",\n"
                + "    \"hint\": \"Focus on semantic differences and how Spring treats each annotation differently for exception translation.\"\n"
                + "  },\n"
                + "  {\n"
                + "    \"question\": \"Tell me about a time you had to debug a production issue under pressure.\",\n"
                + "    \"type\": \"Behavioral\",\n"
                + "    \"difficulty\": \"Medium\",\n"
                + "    \"hint\": \"Use the STAR method: Situation, Task, Action, Result. Highlight your debugging process and communication.\"\n"
                + "  }\n"
                + "]";
    }

    private List<InterviewQuestion> parseQuestions(String cleanedJson) {
        try {
            return objectMapper.readValue(cleanedJson, new TypeReference<List<InterviewQuestion>>() {});
        } catch (Exception e) {
            log.error("Failed to parse Gemini interview response: {}", cleanedJson, e);
            throw new RuntimeException("Gemini returned an unreadable response. Please try again.");
        }
    }
}
