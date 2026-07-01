package com.careerpilot.careerpilot.skill.controller;

import com.careerpilot.careerpilot.skill.dto.SkillExtractionResponse;
import com.careerpilot.careerpilot.skill.service.SkillExtractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/skill")
@RequiredArgsConstructor
public class SkillController {

    private final SkillExtractionService skillExtractionService;

    @PostMapping("/extract/{resumeId}")
    public ResponseEntity<SkillExtractionResponse> extract(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(skillExtractionService.extractSkills(resumeId, userDetails.getUsername()));
    }

    @GetMapping("/resume/{resumeId}")
    public ResponseEntity<SkillExtractionResponse> getByResume(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(skillExtractionService.getSkillsByResume(resumeId, userDetails.getUsername()));
    }
}
