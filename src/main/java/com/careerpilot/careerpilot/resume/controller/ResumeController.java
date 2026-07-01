package com.careerpilot.careerpilot.resume.controller;

import com.careerpilot.careerpilot.resume.dto.ResumeUploadResponse;
import com.careerpilot.careerpilot.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping("/upload")
    public ResponseEntity<ResumeUploadResponse> upload(
            @RequestParam MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(resumeService.upload(file, userDetails.getUsername()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ResumeUploadResponse>> getMyResumes(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(resumeService.getMyResumes(userDetails.getUsername()));
    }
}
