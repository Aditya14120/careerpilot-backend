package com.careerpilot.careerpilot.resume.service;

import com.careerpilot.careerpilot.resume.dto.ResumeUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResumeService {

    ResumeUploadResponse upload(MultipartFile file, String userEmail);

    List<ResumeUploadResponse> getMyResumes(String userEmail);
}
