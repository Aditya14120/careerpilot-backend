package com.careerpilot.careerpilot.career.controller;

import com.careerpilot.careerpilot.career.dto.GapAnalysisRequest;
import com.careerpilot.careerpilot.career.dto.GapAnalysisResponse;
import com.careerpilot.careerpilot.career.service.GapAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/career")
@RequiredArgsConstructor
public class GapAnalysisController {

    private final GapAnalysisService gapAnalysisService;

    @PostMapping("/gap-analysis")
    public ResponseEntity<GapAnalysisResponse> analyze(
            @Valid @RequestBody GapAnalysisRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(gapAnalysisService.analyze(request, userDetails.getUsername()));
    }

    @GetMapping("/gap-analysis/my")
    public ResponseEntity<List<GapAnalysisResponse>> getMyAnalyses(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(gapAnalysisService.getMyAnalyses(userDetails.getUsername()));
    }
}
