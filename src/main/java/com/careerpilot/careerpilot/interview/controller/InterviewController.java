package com.careerpilot.careerpilot.interview.controller;

import com.careerpilot.careerpilot.interview.dto.InterviewRequest;
import com.careerpilot.careerpilot.interview.dto.InterviewResponse;
import com.careerpilot.careerpilot.interview.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping("/generate")
    public ResponseEntity<InterviewResponse> generate(
            @Valid @RequestBody InterviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(interviewService.generate(request, userDetails.getUsername()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<InterviewResponse>> getMySessions(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(interviewService.getMySessions(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InterviewResponse> getSession(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(interviewService.getSession(id, userDetails.getUsername()));
    }
}
